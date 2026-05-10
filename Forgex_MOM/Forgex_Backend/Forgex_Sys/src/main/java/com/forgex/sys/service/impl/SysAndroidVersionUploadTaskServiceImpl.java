package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.SysAndroidVersionDTO;
import com.forgex.sys.domain.dto.SysAndroidVersionUploadCompleteDTO;
import com.forgex.sys.domain.dto.SysAndroidVersionUploadInitDTO;
import com.forgex.sys.domain.entity.SysAndroidVersionUploadTask;
import com.forgex.sys.domain.vo.SysAndroidVersionUploadTaskVO;
import com.forgex.sys.domain.vo.SysAndroidVersionVO;
import com.forgex.sys.mapper.SysAndroidVersionUploadTaskMapper;
import com.forgex.sys.service.ISysAndroidVersionService;
import com.forgex.sys.service.ISysAndroidVersionUploadTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Android APK chunk upload task service implementation.
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysAndroidVersionUploadTaskServiceImpl
        extends ServiceImpl<SysAndroidVersionUploadTaskMapper, SysAndroidVersionUploadTask>
        implements ISysAndroidVersionUploadTaskService {

    private static final String STATUS_UPLOADING = "UPLOADING";
    private static final String STATUS_MERGING = "MERGING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELED = "CANCELED";
    private static final String STATUS_FAILED = "FAILED";
    private static final long DEFAULT_CHUNK_SIZE = 8L * 1024 * 1024;
    private static final long MIN_CHUNK_SIZE = 1024L * 1024;
    private static final long MAX_CHUNK_SIZE = 64L * 1024 * 1024;
    private static final int MAX_CHUNK_COUNT = 20000;

    private final ISysAndroidVersionService androidVersionService;

    @Value("${android-version.upload.temp-dir:${FORGEX_HOME:C:/forgex}/data/tmp/android-version}")
    private String tempRoot;

    @Value("${android-version.upload.expire-hours:24}")
    private int expireHours;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysAndroidVersionUploadTaskVO initUpload(SysAndroidVersionUploadInitDTO dto) throws IOException {
        validateInit(dto);
        SysAndroidVersionUploadTask existing = findReusableTask(dto);
        if (existing != null) {
            return toVO(existing, null);
        }

        String uploadId = UUID.randomUUID().toString().replace("-", "");
        Path taskDir = resolveTaskDir(uploadId);
        Files.createDirectories(taskDir);

        SysAndroidVersionUploadTask task = new SysAndroidVersionUploadTask();
        task.setUploadId(uploadId);
        task.setFileName(dto.getFileName().trim());
        task.setFileSize(dto.getFileSize());
        task.setChunkSize(normalizeChunkSize(dto.getChunkSize()));
        task.setTotalChunks(dto.getTotalChunks());
        task.setUploadedChunks("");
        task.setUploadedCount(0);
        task.setStatus(STATUS_UPLOADING);
        task.setFileHash(normalizeHash(dto.getFileHash()));
        task.setTempDir(taskDir.toString());
        task.setExpireTime(LocalDateTime.now().plusHours(resolveExpireHours()));
        save(task);
        return toVO(task, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysAndroidVersionUploadTaskVO uploadChunk(String uploadId, Integer chunkIndex, MultipartFile file) throws IOException {
        SysAndroidVersionUploadTask task = requireTask(uploadId);
        validateWritableTask(task);
        validateChunk(task, chunkIndex, file);

        Path taskDir = Path.of(task.getTempDir()).toAbsolutePath().normalize();
        Files.createDirectories(taskDir);
        Path target = taskDir.resolve(chunkFileName(chunkIndex)).normalize();
        ensureChildPath(taskDir, target);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        Set<Integer> uploaded = parseChunks(task.getUploadedChunks());
        uploaded.add(chunkIndex);
        task.setUploadedChunks(formatChunks(uploaded));
        task.setUploadedCount(uploaded.size());
        task.setExpireTime(LocalDateTime.now().plusHours(resolveExpireHours()));
        updateById(task);
        return toVO(task, null);
    }

    @Override
    public SysAndroidVersionUploadTaskVO getUploadStatus(String uploadId) {
        return toVO(requireTask(uploadId), null);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public SysAndroidVersionVO completeUpload(SysAndroidVersionUploadCompleteDTO dto) throws IOException {
        validateComplete(dto);
        SysAndroidVersionUploadTask task = requireTask(dto.getUploadId());
        if (STATUS_COMPLETED.equals(task.getStatus()) && task.getVersionId() != null) {
            return androidVersionService.getVersion(task.getVersionId());
        }
        validateWritableTask(task);
        Set<Integer> uploaded = parseChunks(task.getUploadedChunks());
        if (uploaded.size() != task.getTotalChunks()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED, "分片尚未上传完成");
        }

        task.setStatus(STATUS_MERGING);
        task.setVersionCode(dto.getVersionCode());
        task.setVersionName(dto.getVersionName());
        task.setChangelog(dto.getChangelog());
        updateById(task);

        Path mergedFile = mergeChunks(task);
        verifyMergedFile(task, mergedFile);

        SysAndroidVersionDTO versionDTO = new SysAndroidVersionDTO();
        versionDTO.setVersionCode(dto.getVersionCode());
        versionDTO.setVersionName(dto.getVersionName());
        versionDTO.setChangelog(dto.getChangelog());
        versionDTO.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        SysAndroidVersionVO version = androidVersionService.uploadApk(
                new PathMultipartFile("file", task.getFileName(), "application/vnd.android.package-archive", mergedFile),
                versionDTO
        );

        task.setMergedFilePath(mergedFile.toString());
        task.setFinalFileUrl(version.getFileUrl());
        task.setVersionId(version.getId());
        task.setStatus(STATUS_COMPLETED);
        task.setErrorMessage(null);
        updateById(task);
        cleanupTaskFiles(task);
        return version;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUpload(String uploadId) throws IOException {
        SysAndroidVersionUploadTask task = requireTask(uploadId);
        if (!STATUS_COMPLETED.equals(task.getStatus())) {
            task.setStatus(STATUS_CANCELED);
            updateById(task);
            cleanupTaskFiles(task);
        }
    }

    @Override
    @Scheduled(cron = "${android-version.upload.cleanup-cron:0 0 * * * ?}")
    public void cleanupExpiredTasks() {
        List<SysAndroidVersionUploadTask> expired = list(new LambdaQueryWrapper<SysAndroidVersionUploadTask>()
                .in(SysAndroidVersionUploadTask::getStatus, STATUS_UPLOADING, STATUS_FAILED, STATUS_CANCELED)
                .lt(SysAndroidVersionUploadTask::getExpireTime, LocalDateTime.now()));
        for (SysAndroidVersionUploadTask task : expired) {
            try {
                cleanupTaskFiles(task);
                if (!STATUS_CANCELED.equals(task.getStatus())) {
                    task.setStatus(STATUS_CANCELED);
                    updateById(task);
                }
            } catch (IOException e) {
                log.warn("Clean expired android upload task failed, uploadId={}", task.getUploadId(), e);
            }
        }
    }

    private SysAndroidVersionUploadTask findReusableTask(SysAndroidVersionUploadInitDTO dto) {
        String fileHash = normalizeHash(dto.getFileHash());
        if (!StringUtils.hasText(fileHash)) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<SysAndroidVersionUploadTask>()
                .eq(SysAndroidVersionUploadTask::getFileHash, fileHash)
                .eq(SysAndroidVersionUploadTask::getFileName, dto.getFileName().trim())
                .eq(SysAndroidVersionUploadTask::getFileSize, dto.getFileSize())
                .eq(SysAndroidVersionUploadTask::getStatus, STATUS_UPLOADING)
                .gt(SysAndroidVersionUploadTask::getExpireTime, LocalDateTime.now())
                .orderByDesc(SysAndroidVersionUploadTask::getCreateTime)
                .last("LIMIT 1"));
    }

    private Path mergeChunks(SysAndroidVersionUploadTask task) throws IOException {
        Path taskDir = Path.of(task.getTempDir()).toAbsolutePath().normalize();
        Path mergedFile = taskDir.resolve(safeFileName(task.getFileName())).normalize();
        ensureChildPath(taskDir, mergedFile);
        Files.deleteIfExists(mergedFile);
        try (var output = Files.newOutputStream(mergedFile)) {
            for (int i = 0; i < task.getTotalChunks(); i++) {
                Path chunk = taskDir.resolve(chunkFileName(i)).normalize();
                ensureChildPath(taskDir, chunk);
                if (!Files.exists(chunk)) {
                    throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED, "缺少分片：" + i);
                }
                Files.copy(chunk, output);
            }
        }
        return mergedFile;
    }

    private void verifyMergedFile(SysAndroidVersionUploadTask task, Path mergedFile) throws IOException {
        long size = Files.size(mergedFile);
        if (size != task.getFileSize()) {
            markFailed(task, "合并后文件大小不一致");
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED, "合并后文件大小不一致");
        }
        if (StringUtils.hasText(task.getFileHash())) {
            String actual = sha256(mergedFile);
            if (!task.getFileHash().equalsIgnoreCase(actual)) {
                markFailed(task, "文件校验失败");
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED, "文件校验失败");
            }
        }
    }

    private void markFailed(SysAndroidVersionUploadTask task, String message) {
        task.setStatus(STATUS_FAILED);
        task.setErrorMessage(message);
        updateById(task);
    }

    private SysAndroidVersionUploadTask requireTask(String uploadId) {
        if (!StringUtils.hasText(uploadId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        SysAndroidVersionUploadTask task = getOne(new LambdaQueryWrapper<SysAndroidVersionUploadTask>()
                .eq(SysAndroidVersionUploadTask::getUploadId, uploadId)
                .last("LIMIT 1"));
        if (task == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED, "上传任务不存在");
        }
        return task;
    }

    private void validateInit(SysAndroidVersionUploadInitDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getFileName()) || dto.getFileSize() == null
                || dto.getFileSize() <= 0 || dto.getTotalChunks() == null || dto.getTotalChunks() <= 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        if (!dto.getFileName().trim().toLowerCase(Locale.ROOT).endsWith(".apk")) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED, "只能上传 APK 文件");
        }
        if (dto.getTotalChunks() > MAX_CHUNK_COUNT) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED, "分片数量过多");
        }
    }

    private void validateWritableTask(SysAndroidVersionUploadTask task) {
        if (!STATUS_UPLOADING.equals(task.getStatus())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED, "上传任务状态不允许写入");
        }
        if (task.getExpireTime() != null && task.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED, "上传任务已过期");
        }
    }

    private void validateChunk(SysAndroidVersionUploadTask task, Integer chunkIndex, MultipartFile file) {
        if (chunkIndex == null || chunkIndex < 0 || chunkIndex >= task.getTotalChunks() || file == null || file.isEmpty()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        long maxSize = Math.min(MAX_CHUNK_SIZE, task.getChunkSize() + 1024L);
        if (file.getSize() > maxSize) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED, "单个分片过大");
        }
    }

    private void validateComplete(SysAndroidVersionUploadCompleteDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getUploadId()) || dto.getVersionCode() == null
                || !StringUtils.hasText(dto.getVersionName())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
    }

    private SysAndroidVersionUploadTaskVO toVO(SysAndroidVersionUploadTask task, SysAndroidVersionVO version) {
        SysAndroidVersionUploadTaskVO vo = new SysAndroidVersionUploadTaskVO();
        BeanUtils.copyProperties(task, vo);
        List<Integer> uploaded = new ArrayList<>(parseChunks(task.getUploadedChunks()));
        List<Integer> missing = new ArrayList<>();
        for (int i = 0; i < task.getTotalChunks(); i++) {
            if (!uploaded.contains(i)) {
                missing.add(i);
            }
        }
        vo.setUploadedChunks(uploaded);
        vo.setMissingChunks(missing);
        vo.setVersion(version);
        return vo;
    }

    private Set<Integer> parseChunks(String value) {
        Set<Integer> chunks = new TreeSet<>();
        if (!StringUtils.hasText(value)) {
            return chunks;
        }
        for (String item : value.split(",")) {
            if (StringUtils.hasText(item)) {
                chunks.add(Integer.parseInt(item.trim()));
            }
        }
        return chunks;
    }

    private String formatChunks(Set<Integer> chunks) {
        return String.join(",", chunks.stream().map(String::valueOf).toList());
    }

    private String chunkFileName(int chunkIndex) {
        return String.format(Locale.ROOT, "%08d.part", chunkIndex);
    }

    private Long normalizeChunkSize(Long chunkSize) {
        if (chunkSize == null || chunkSize <= 0) {
            return DEFAULT_CHUNK_SIZE;
        }
        return Math.max(MIN_CHUNK_SIZE, Math.min(MAX_CHUNK_SIZE, chunkSize));
    }

    private String normalizeHash(String hash) {
        return StringUtils.hasText(hash) ? hash.trim().toLowerCase(Locale.ROOT) : "";
    }

    private int resolveExpireHours() {
        return Math.max(1, expireHours);
    }

    private Path resolveTaskDir(String uploadId) {
        Path root = Path.of(tempRoot).toAbsolutePath().normalize();
        return root.resolve(uploadId).normalize();
    }

    private void ensureChildPath(Path parent, Path child) throws IOException {
        Path normalizedParent = parent.toAbsolutePath().normalize();
        Path normalizedChild = child.toAbsolutePath().normalize();
        if (!normalizedChild.startsWith(normalizedParent)) {
            throw new IOException("Invalid upload path");
        }
    }

    private String safeFileName(String fileName) {
        String value = StringUtils.hasText(fileName) ? fileName.trim() : "android.apk";
        value = value.replace('\\', '/');
        int index = value.lastIndexOf('/');
        return index >= 0 ? value.substring(index + 1) : value;
    }

    private String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(file); DigestInputStream dis = new DigestInputStream(input, digest)) {
                byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) {
                    // read stream to update digest
                }
            }
            StringBuilder builder = new StringBuilder();
            for (byte b : digest.digest()) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 not supported", e);
        }
    }

    private void cleanupTaskFiles(SysAndroidVersionUploadTask task) throws IOException {
        if (!StringUtils.hasText(task.getTempDir())) {
            return;
        }
        Path dir = Path.of(task.getTempDir()).toAbsolutePath().normalize();
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(dir)) {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static class PathMultipartFile implements MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final Path path;

        private PathMultipartFile(String name, String originalFilename, String contentType, Path path) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.path = path;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return getSize() <= 0;
        }

        @Override
        public long getSize() {
            try {
                return Files.size(path);
            } catch (IOException e) {
                return 0;
            }
        }

        @Override
        public byte[] getBytes() throws IOException {
            return Files.readAllBytes(path);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(path);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            Files.copy(path, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

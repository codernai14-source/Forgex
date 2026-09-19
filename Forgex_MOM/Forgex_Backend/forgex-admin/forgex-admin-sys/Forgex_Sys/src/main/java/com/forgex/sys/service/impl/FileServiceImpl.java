package com.forgex.sys.service.impl;

import com.forgex.common.config.ConfigService;
import com.forgex.sys.domain.config.FileUploadConfig;
import com.forgex.sys.service.FileService;
import com.forgex.sys.service.ISysFileRecordService;
import com.forgex.sys.service.storage.FileStorageFactory;
import com.forgex.sys.service.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件上传与公开读取实现。
 * <p>
 * 读取时返回 {@link FileSystemResource}，保证响应带有真实文件名，便于推断图片/视频 Content-Type。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FileService
 */
@Service
public class FileServiceImpl implements FileService {
    private static final String KEY_FILE_UPLOAD = "file.upload.settings";

    @Value("${file.upload.path:C:/forgex/data/uploads}")
    private String uploadPath;

    @jakarta.annotation.Resource
    private ConfigService configService;

    @jakarta.annotation.Resource
    private FileStorageFactory fileStorageFactory;

    @jakarta.annotation.Resource
    private ISysFileRecordService fileRecordService;

    private String resolveUploadPath() {
        FileUploadConfig cfg = configService.getGlobalJson(KEY_FILE_UPLOAD, FileUploadConfig.class, null);
        if (cfg != null && StringUtils.hasText(cfg.getLocalUploadPath())) {
            return cfg.getLocalUploadPath();
        }
        return uploadPath;
    }

    /**
     * 获取本位dir。
     *
     * @return 处理结果
     */
    @Override
    public Path getBaseDir() {
        return Paths.get(resolveUploadPath());
    }

    /**
     * 上传文件。
     *
     * @param file 文件
     * @param moduleCode 模块编码
     * @param moduleName 模块名称
     * @return 字符串结果
     */
    @Override
    public String upload(MultipartFile file, String moduleCode, String moduleName) throws IOException {
        return upload(file, moduleCode, moduleName, null);
    }

    /**
     * 按可选体积上限上传并记录文件。
     *
     * @param file 上传文件
     * @param moduleCode 模块编码
     * @param moduleName 模块名称
     * @param maxSizeMbOverride 覆盖体积上限（MB）
     * @return 访问 URL
     * @throws IOException 存储失败时抛出
     */
    @Override
    public String upload(MultipartFile file, String moduleCode, String moduleName, Long maxSizeMbOverride) throws IOException {
        FileStorageService storage = fileStorageFactory.getDefault();
        String relativePath = storage.upload(file, maxSizeMbOverride);
        String accessUrl = storage.getUrl(relativePath);
        fileRecordService.saveUploadRecord(
                file,
                moduleCode,
                moduleName,
                relativePath,
                accessUrl,
                fileStorageFactory.resolveStorageType(),
                fileStorageFactory.resolveStorageConfigId()
        );
        return accessUrl;
    }

    /**
     * 读取本地公开文件。
     * <p>
     * 返回 {@link FileSystemResource}，以便响应层能拿到真实文件名并推断 Content-Type。
     * 开启 {@code X-Content-Type-Options: nosniff} 后，缺少文件名的流资源会被当成
     * {@code application/octet-stream}，浏览器将拒绝把 PNG/JPG/MP4 当图片或视频渲染。
     * </p>
     *
     * @param filename 相对文件名，通常为 UUID 加扩展名
     * @return 文件资源；不存在时返回 {@code null}
     * @throws IOException 路径越出上传根目录时抛出
     */
    @Override
    public Resource getFile(String filename) throws IOException {
        Path dir = getBaseDir().toAbsolutePath().normalize();
        Path filePath = dir.resolve(filename).normalize();
        if (!filePath.startsWith(dir)) {
            throw new IOException("Invalid file path");
        }
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return null;
        }
        return new FileSystemResource(filePath.toFile());
    }

    /**
     * 获取media类型。
     *
     * @param filename filename
     * @return 字符串结果
     */
    @Override
    public String getMediaType(String filename) throws IOException {
        Path dir = getBaseDir();
        dir = dir.toAbsolutePath().normalize();
        Path filePath = dir.resolve(filename).normalize();
        if (!filePath.startsWith(dir)) {
            return "application/octet-stream";
        }
        if (!Files.exists(filePath)) {
            return "application/octet-stream";
        }
        String mediaType = Files.probeContentType(filePath);
        return mediaType == null ? "application/octet-stream" : mediaType;
    }
}

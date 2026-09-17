package com.forgex.sys.service.storage;

import com.forgex.common.config.ConfigService;
import com.forgex.sys.domain.config.FileUploadConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Local file storage service.
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Service
public class LocalStorageService implements FileStorageService {
    private static final String KEY_FILE_UPLOAD = "file.upload.settings";
    private static final int DEFAULT_GATEWAY_PORT = 9000;
    private static final String DEFAULT_GATEWAY_API_PREFIX = "/api";

    @Value("${file.upload.path:C:/forgex/data/uploads}")
    private String uploadPath;

    @Value("${file.access.prefix:/sys/files}")
    private String accessPrefix;

    @Value("${forgex.gateway.port:9000}")
    private int gatewayPort;

    @jakarta.annotation.Resource
    private ConfigService configService;

    private FileUploadConfig loadConfig() {
        return configService.getGlobalJson(KEY_FILE_UPLOAD, FileUploadConfig.class, null);
    }

    private String resolveUploadPath() {
        FileUploadConfig cfg = loadConfig();
        if (cfg != null && StringUtils.hasText(cfg.getLocalUploadPath())) {
            return cfg.getLocalUploadPath();
        }
        return uploadPath;
    }

    private String resolveAccessPrefix() {
        FileUploadConfig cfg = loadConfig();
        String prefix;
        if (cfg != null && StringUtils.hasText(cfg.getAccessPrefix())) {
            prefix = cfg.getAccessPrefix().trim();
        } else {
            prefix = accessPrefix;
        }
        if ("/files".equals(prefix) || "files".equals(prefix) || "/api/files".equals(prefix) || "api/files".equals(prefix)) {
            prefix = "/sys/files";
        }
        while (prefix.endsWith("/") && prefix.length() > 1) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix.startsWith("/") ? prefix : "/" + prefix;
    }

    private String resolvePublicBaseUrl() {
        FileUploadConfig cfg = loadConfig();
        if (cfg != null && StringUtils.hasText(cfg.getPublicBaseUrl())) {
            return normalizePublicBaseUrl(cfg.getPublicBaseUrl());
        }
        return "";
    }

    private String normalizePublicBaseUrl(String rawValue) {
        String value = rawValue.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = "http://" + value;
        }
        try {
            URI uri = new URI(value);
            String scheme = StringUtils.hasText(uri.getScheme()) ? uri.getScheme() : "http";
            String host = uri.getHost();
            if (!StringUtils.hasText(host)) {
                return value;
            }
            int port = uri.getPort() > 0 ? uri.getPort() : resolveGatewayPort();
            String path = StringUtils.hasText(uri.getPath()) ? uri.getPath() : "";
            if (port == resolveGatewayPort() && !path.equals(DEFAULT_GATEWAY_API_PREFIX) && !path.startsWith(DEFAULT_GATEWAY_API_PREFIX + "/")) {
                path = DEFAULT_GATEWAY_API_PREFIX + path;
            }
            while (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }
            return new URI(scheme, null, host, port, path, null, null).toString();
        } catch (URISyntaxException e) {
            return value;
        }
    }

    private int resolveGatewayPort() {
        return gatewayPort > 0 ? gatewayPort : DEFAULT_GATEWAY_PORT;
    }

    /**
     * 上传文件。
     *
     * @param file 文件
     * @return 字符串结果
     */
    @Override
    public String upload(MultipartFile file) throws IOException {
        return upload(file, null);
    }

    /**
     * 按可选体积上限上传本地文件。
     *
     * @param file 上传文件
     * @param maxSizeMbOverride 覆盖体积上限（MB）；为空时使用全局配置
     * @return 相对路径
     * @throws IOException 文件为空、超限或扩展名不在白名单时抛出
     */
    @Override
    public String upload(MultipartFile file, Long maxSizeMbOverride) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        FileUploadConfig cfg = loadConfig();
        long configuredMb = cfg == null ? 20L : Math.max(1L, cfg.getMaxSizeMb());
        long maxMb = maxSizeMbOverride != null && maxSizeMbOverride > 0 ? maxSizeMbOverride : configuredMb;
        long maxBytes = maxMb * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new IOException("file exceeds configured size limit");
        }
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String extension = fileExtension.startsWith(".") ? fileExtension.substring(1).toLowerCase(java.util.Locale.ROOT) : "";
        java.util.Set<String> allowed = cfg == null ? java.util.Set.of("jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt", "zip") : cfg.getAllowedExtensions();
        if (!allowed.isEmpty() && !allowed.stream().map(v -> v.toLowerCase(java.util.Locale.ROOT)).collect(java.util.stream.Collectors.toSet()).contains(extension)) {
            throw new IOException("file extension is not allowed");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + fileExtension;
        String relativePath = fileName;

        File targetDir = new File(resolveUploadPath());
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("Failed to create upload directory: " + targetDir.getAbsolutePath());
        }

        File targetFile = new File(targetDir, fileName);
        file.transferTo(targetFile);
        return relativePath;
    }

    /**
     * 下载文件。
     *
     * @param filePath 文件路径
     * @param response 响应数据
     */
    @Override
    public void download(String filePath, HttpServletResponse response) throws IOException {
        if (!StringUtils.hasText(filePath)) {
            throw new IOException("filePath must not be empty");
        }
        Path root = Paths.get(resolveUploadPath()).toAbsolutePath().normalize();
        Path resolved = root.resolve(filePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IOException("invalid file path");
        }
        File file = resolved.toFile();
        if (!file.exists() || !file.isFile()) {
            throw new IOException("file does not exist");
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        response.setContentLengthLong(file.length());

        try (FileInputStream fis = new FileInputStream(file); OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }

    /**
     * 删除数据。
     *
     * @param filePath 文件路径
     * @return 是否处理成功
     */
    @Override
    public boolean delete(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }
        Path root = Paths.get(resolveUploadPath()).toAbsolutePath().normalize();
        Path resolved = root.resolve(filePath).normalize();
        if (!resolved.startsWith(root)) {
            return false;
        }
        File file = resolved.toFile();
        return file.exists() && file.delete();
    }

    /**
     * 获取url。
     *
     * @param filePath 文件路径
     * @return 字符串结果
     */
    @Override
    public String getUrl(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        String prefix = resolveAccessPrefix();
        if (!StringUtils.hasText(prefix)) {
            prefix = "/sys/files";
        }
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        String publicBaseUrl = resolvePublicBaseUrl();
        if (StringUtils.hasText(publicBaseUrl)) {
            return publicBaseUrl + prefix + "/" + filePath;
        }
        return prefix + "/" + filePath;
    }
}

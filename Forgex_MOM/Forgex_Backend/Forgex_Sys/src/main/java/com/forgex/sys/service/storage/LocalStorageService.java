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

/**
 * Local file storage service.
 */
@Service
public class LocalStorageService implements FileStorageService {
    private static final String KEY_FILE_UPLOAD = "file.upload.settings";
    private static final int DEFAULT_GATEWAY_PORT = 9000;
    private static final String DEFAULT_GATEWAY_API_PREFIX = "/api";

    @Value("${file.upload.path:C:/forgex/data/uploads}")
    private String uploadPath;

    @Value("${file.access.prefix:/files}")
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

    @Override
    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
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

    @Override
    public void download(String filePath, HttpServletResponse response) throws IOException {
        if (!StringUtils.hasText(filePath)) {
            throw new IOException("filePath must not be empty");
        }
        File file = new File(resolveUploadPath(), filePath);
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

    @Override
    public boolean delete(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }
        File file = new File(resolveUploadPath(), filePath);
        return file.exists() && file.delete();
    }

    @Override
    public String getUrl(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        String prefix = resolveAccessPrefix();
        if (!StringUtils.hasText(prefix)) {
            prefix = "/files";
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

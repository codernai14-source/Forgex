package com.forgex.sys.service.impl;

import com.forgex.common.config.ConfigService;
import com.forgex.sys.domain.config.FileUploadConfig;
import com.forgex.sys.service.FileService;
import com.forgex.sys.service.ISysFileRecordService;
import com.forgex.sys.service.storage.FileStorageFactory;
import com.forgex.sys.service.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File service implementation.
 */
@Service
public class FileServiceImpl implements FileService {
    private static final String KEY_FILE_UPLOAD = "file.upload.settings";

    @Value("${file.upload.path:./uploads}")
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

    @Override
    public Path getBaseDir() {
        return Paths.get(resolveUploadPath());
    }

    @Override
    public String upload(MultipartFile file, String moduleCode, String moduleName) throws IOException {
        FileStorageService storage = fileStorageFactory.getDefault();
        String relativePath = storage.upload(file);
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

    @Override
    public Resource getFile(String filename) throws IOException {
        Path dir = getBaseDir();
        Path filePath = dir.resolve(filename).normalize();
        if (!Files.exists(filePath)) {
            return null;
        }
        return new InputStreamResource(Files.newInputStream(filePath));
    }

    @Override
    public String getMediaType(String filename) throws IOException {
        Path dir = getBaseDir();
        Path filePath = dir.resolve(filename).normalize();
        if (!Files.exists(filePath)) {
            return "application/octet-stream";
        }
        String mediaType = Files.probeContentType(filePath);
        return mediaType == null ? "application/octet-stream" : mediaType;
    }
}

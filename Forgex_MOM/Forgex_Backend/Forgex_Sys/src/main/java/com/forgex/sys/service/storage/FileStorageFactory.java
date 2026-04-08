package com.forgex.sys.service.storage;

import com.forgex.common.config.ConfigService;
import com.forgex.sys.domain.config.FileUploadConfig;
import com.forgex.sys.domain.entity.SysFileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * File storage factory.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageFactory {
    private static final String KEY_FILE_UPLOAD = "file.upload.settings";

    private final ConfigService configService;
    private final SysFileStorageConfigService storageConfigService;
    private final LocalStorageService localStorageService;

    /**
     * Resolve storage service with priority:
     * 1) global system config key: file.upload.settings
     * 2) tenant table config: sys_file_storage
     * 3) fallback local storage
     */
    public FileStorageService getDefault() {
        FileUploadConfig uploadConfig = configService.getGlobalJson(KEY_FILE_UPLOAD, FileUploadConfig.class, null);
        if (uploadConfig != null && StringUtils.hasText(uploadConfig.getStorageType())) {
            String type = uploadConfig.getStorageType().trim().toUpperCase();
            if ("LOCAL".equals(type) || StringUtils.hasText(uploadConfig.getProviderConfigJson())) {
                return getByType(type, uploadConfig.getProviderConfigJson());
            }
            log.warn("file.upload.settings ignored because providerConfigJson is empty for storageType={}", type);
        }

        SysFileStorage cfg = storageConfigService.getDefault();
        if (cfg == null || !StringUtils.hasText(cfg.getStorageType())) {
            log.info("No file storage config found, using local storage");
            return localStorageService;
        }
        return getByType(cfg.getStorageType(), cfg.getConfigJson());
    }

    /**
     * Build storage service by storage type.
     */
    public FileStorageService getByType(String storageType, String configJson) {
        if (!StringUtils.hasText(storageType)) {
            throw new IllegalArgumentException("storageType must not be empty");
        }

        String type = storageType.trim().toUpperCase();
        try {
            switch (type) {
                case "LOCAL":
                    return localStorageService;
                case "OSS":
                    if (!StringUtils.hasText(configJson)) {
                        throw new IllegalArgumentException("OSS configJson must not be empty");
                    }
                    return new OssStorageService(configJson);
                case "MINIO":
                    if (!StringUtils.hasText(configJson)) {
                        throw new IllegalArgumentException("MINIO configJson must not be empty");
                    }
                    return new MinioStorageService(configJson);
                default:
                    throw new UnsupportedOperationException("Unsupported storageType: " + type);
            }
        } catch (IOException e) {
            throw new RuntimeException("Create storage service failed: " + e.getMessage(), e);
        }
    }
}

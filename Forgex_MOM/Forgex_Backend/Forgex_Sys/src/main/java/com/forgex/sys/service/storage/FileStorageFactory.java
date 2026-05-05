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
 *
 * @author Forgex Team
 * @version 1.0.0
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
     * 获取默认。
     *
     * @return 处理结果
     */
    public FileStorageService getDefault() {
        StorageResolved resolved = resolveStorage();
        return getByType(resolved.storageType, resolved.configJson);
    }

    /**
     * 解析文件存储类型。
     *
     * @return 字符串结果
     */
    public String resolveStorageType() {
        return resolveStorage().storageType;
    }

    /**
     * 解析文件存储配置 ID。
     *
     * @return 数据主键 ID
     */
    public Long resolveStorageConfigId() {
        return resolveStorage().storageConfigId;
    }

    /**
     * 获取by类型。
     *
     * @param storageType storage类型
     * @param configJson 配置json
     * @return 处理结果
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

    private StorageResolved resolveStorage() {
        FileUploadConfig uploadConfig = configService.getGlobalJson(KEY_FILE_UPLOAD, FileUploadConfig.class, null);
        if (uploadConfig != null && StringUtils.hasText(uploadConfig.getStorageType())) {
            String type = uploadConfig.getStorageType().trim().toUpperCase();
            if ("LOCAL".equals(type) || StringUtils.hasText(uploadConfig.getProviderConfigJson())) {
                return new StorageResolved(type, uploadConfig.getProviderConfigJson(), null);
            }
            log.warn("file.upload.settings ignored because providerConfigJson is empty for storageType={}", type);
        }

        SysFileStorage cfg = storageConfigService.getDefault();
        if (cfg == null || !StringUtils.hasText(cfg.getStorageType())) {
            log.info("No file storage config found, using local storage");
            return new StorageResolved("LOCAL", null, null);
        }
        return new StorageResolved(cfg.getStorageType().trim().toUpperCase(), cfg.getConfigJson(), cfg.getId());
    }

    private static class StorageResolved {
        private final String storageType;
        private final String configJson;
        private final Long storageConfigId;

        private StorageResolved(String storageType, String configJson, Long storageConfigId) {
            this.storageType = storageType;
            this.configJson = configJson;
            this.storageConfigId = storageConfigId;
        }
    }
}

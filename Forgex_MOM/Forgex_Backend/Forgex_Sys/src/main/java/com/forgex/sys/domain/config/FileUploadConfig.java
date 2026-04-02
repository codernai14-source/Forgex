package com.forgex.sys.domain.config;

import lombok.Data;

/**
 * File upload configuration.
 */
@Data
public class FileUploadConfig {

    /**
     * LOCAL/OSS/MINIO
     */
    private String storageType = "LOCAL";

    /**
     * Local upload path, only used when storageType=LOCAL.
     */
    private String localUploadPath = "./uploads";

    /**
     * URL access prefix for local files.
     */
    private String accessPrefix = "/files";

    /**
     * Provider config json for OSS/MINIO (placeholder for now).
     */
    private String providerConfigJson = "";

    public static FileUploadConfig defaults() {
        return new FileUploadConfig();
    }
}

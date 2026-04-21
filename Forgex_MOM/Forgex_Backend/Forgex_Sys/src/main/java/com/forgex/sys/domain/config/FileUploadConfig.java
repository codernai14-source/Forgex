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
     * Public base URL for local file access, such as http://192.168.1.10:8082.
     */
    private String publicBaseUrl = "";

    /**
     * Provider config json for OSS/MINIO (placeholder for now).
     */
    private String providerConfigJson = "";

    public static FileUploadConfig defaults() {
        return new FileUploadConfig();
    }
}

package com.forgex.sys.domain.config;

import lombok.Data;

/**
 * File upload configuration.
 *
 * @author Forgex Team
 * @version 1.0.0
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
    private String localUploadPath = "C:/forgex/data/uploads";

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

    /**
     * 获取默认配置。
     *
     * @return 处理结果
     */
    public static FileUploadConfig defaults() {
        return new FileUploadConfig();
    }
}

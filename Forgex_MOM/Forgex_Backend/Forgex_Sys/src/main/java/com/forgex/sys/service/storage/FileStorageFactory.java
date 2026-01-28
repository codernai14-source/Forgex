package com.forgex.sys.service.storage;

import com.forgex.sys.domain.entity.SysFileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 文件存储工厂类
 * <p>根据配置创建对应的文件存储服务实例。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageFactory {

    /**
     * 文件存储配置服务
     */
    private final SysFileStorageConfigService configService;

    /**
     * 本地存储服务
     */
    private final LocalStorageService localStorageService;

    /**
     * 获取默认的文件存储服务
     * <p>根据配置返回对应的存储服务实例。</p>
     * 
     * @return 文件存储服务实例
     * @throws RuntimeException 不支持的存储类型或配置错误
     */
    public FileStorageService getDefault() {
        SysFileStorage cfg = configService.getDefault();
        if (cfg == null || !StringUtils.hasText(cfg.getStorageType())) {
            log.info("未找到文件存储配置，使用本地存储");
            return localStorageService;
        }
        
        String type = cfg.getStorageType().trim().toUpperCase();
        log.info("使用文件存储类型: {}", type);
        
        try {
            switch (type) {
                case "LOCAL":
                    return localStorageService;
                case "OSS":
                    return new OssStorageService(cfg.getConfigJson());
                case "MINIO":
                    return new MinioStorageService(cfg.getConfigJson());
                default:
                    throw new UnsupportedOperationException("不支持的存储类型: " + type);
            }
        } catch (IOException e) {
            log.error("创建文件存储服务失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建文件存储服务失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据存储类型获取文件存储服务
     * 
     * @param storageType 存储类型（LOCAL/OSS/MINIO）
     * @param configJson 配置JSON（LOCAL类型可为空）
     * @return 文件存储服务实例
     * @throws RuntimeException 不支持的存储类型或配置错误
     */
    public FileStorageService getByType(String storageType, String configJson) {
        if (!StringUtils.hasText(storageType)) {
            throw new IllegalArgumentException("存储类型不能为空");
        }
        
        String type = storageType.trim().toUpperCase();
        
        try {
            switch (type) {
                case "LOCAL":
                    return localStorageService;
                case "OSS":
                    if (!StringUtils.hasText(configJson)) {
                        throw new IllegalArgumentException("OSS配置不能为空");
                    }
                    return new OssStorageService(configJson);
                case "MINIO":
                    if (!StringUtils.hasText(configJson)) {
                        throw new IllegalArgumentException("MinIO配置不能为空");
                    }
                    return new MinioStorageService(configJson);
                default:
                    throw new UnsupportedOperationException("不支持的存储类型: " + type);
            }
        } catch (IOException e) {
            log.error("创建文件存储服务失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建文件存储服务失败: " + e.getMessage(), e);
        }
    }
}


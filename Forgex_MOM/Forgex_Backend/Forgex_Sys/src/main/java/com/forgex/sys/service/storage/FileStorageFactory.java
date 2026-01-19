package com.forgex.sys.service.storage;

import com.forgex.sys.domain.entity.SysFileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 文件存储工厂类
 * <p>根据配置创建对应的文件存储服务实例。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
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
     * @throws UnsupportedOperationException 不支持的存储类型
     */
    public FileStorageService getDefault() {
        SysFileStorage cfg = configService.getDefault();
        if (cfg == null || !StringUtils.hasText(cfg.getStorageType())) {
            return localStorageService;
        }
        String type = cfg.getStorageType().trim().toUpperCase();
        if ("LOCAL".equals(type)) {
            return localStorageService;
        }
        throw new UnsupportedOperationException("不支持的存储类型: " + type);
    }
}


package com.forgex.common.config;

import com.forgex.common.domain.config.PersonalHomepageConfig;

/**
 * 个人首页配置服务。
 */
public interface PersonalHomepageConfigService {

    /**
     * 读取当前用户生效的个人首页配置。
     */
    PersonalHomepageConfig getEffectiveConfig(Long userId, Long tenantId);

    /**
     * 读取用户级配置。
     */
    PersonalHomepageConfig getUserConfig(Long userId, Long tenantId);

    /**
     * 保存用户级配置。
     */
    void saveUserConfig(Long userId, Long tenantId, PersonalHomepageConfig config);

    /**
     * 重置用户级配置。
     */
    boolean resetUserConfig(Long userId, Long tenantId);

    /**
     * 读取租户级配置。
     */
    PersonalHomepageConfig getTenantConfig(Long tenantId);

    /**
     * 保存租户级配置。
     */
    void saveTenantConfig(Long tenantId, PersonalHomepageConfig config);

    /**
     * 读取公共级配置。
     */
    PersonalHomepageConfig getPublicConfig();

    /**
     * 保存公共级配置。
     */
    void savePublicConfig(PersonalHomepageConfig config);
}

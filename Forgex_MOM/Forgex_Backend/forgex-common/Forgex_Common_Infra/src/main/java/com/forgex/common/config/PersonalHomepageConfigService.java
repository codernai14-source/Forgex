package com.forgex.common.config;

import com.forgex.common.domain.config.PersonalHomepageConfig;

/**
 * 个人与模块首页配置服务。
 * <p>
 * 空模块编码保持个人首页历史配置键，非空模块编码用于基础信息、审批管理、系统管理等模块首页配置。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface PersonalHomepageConfigService {

    /**
     * 读取当前用户生效的个人首页配置。
     */
    default PersonalHomepageConfig getEffectiveConfig(Long userId, Long tenantId) {
        /**
         * 获取effective配置。
         *
         * @return 处理结果
         */
        return getEffectiveConfig(userId, tenantId, null);
    }

    /**
     * 读取当前用户在指定模块下生效的首页配置。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param moduleCode 模块编码，空值保持个人首页兼容逻辑
     * @return 首页配置
     */
    PersonalHomepageConfig getEffectiveConfig(Long userId, Long tenantId, String moduleCode);

    /**
     * 读取用户级个人首页配置。
     */
    default PersonalHomepageConfig getUserConfig(Long userId, Long tenantId) {
        /**
         * 获取用户配置。
         *
         * @return 处理结果
         */
        return getUserConfig(userId, tenantId, null);
    }

    /**
     * 读取用户级模块首页配置。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param moduleCode 模块编码
     * @return 首页配置
     */
    PersonalHomepageConfig getUserConfig(Long userId, Long tenantId, String moduleCode);

    /**
     * 保存用户级个人首页配置。
     */
    default void saveUserConfig(Long userId, Long tenantId, PersonalHomepageConfig config) {
      /**
       * 保存用户配置。
       */
        saveUserConfig(userId, tenantId, null, config);
    }

    /**
     * 保存用户级模块首页配置。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param moduleCode 模块编码
     * @param config 首页配置
     */
    void saveUserConfig(Long userId, Long tenantId, String moduleCode, PersonalHomepageConfig config);

    /**
     * 重置用户级个人首页配置。
     */
    default boolean resetUserConfig(Long userId, Long tenantId) {
        /**
         * 执行personal首页配置的reset用户配置操作。
         *
         * @return 处理结果
         */
        return resetUserConfig(userId, tenantId, null);
    }

    /**
     * 重置用户级模块首页配置。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param moduleCode 模块编码
     * @return 是否重置成功
     */
    boolean resetUserConfig(Long userId, Long tenantId, String moduleCode);

    /**
     * 读取租户级个人首页配置。
     */
    default PersonalHomepageConfig getTenantConfig(Long tenantId) {
        /**
         * 获取租户配置。
         *
         * @return 处理结果
         */
        return getTenantConfig(tenantId, null);
    }

    /**
     * 读取租户级模块首页配置。
     *
     * @param tenantId 租户 ID
     * @param moduleCode 模块编码
     * @return 首页配置
     */
    PersonalHomepageConfig getTenantConfig(Long tenantId, String moduleCode);

    /**
     * 保存租户级个人首页配置。
     */
    default void saveTenantConfig(Long tenantId, PersonalHomepageConfig config) {
      /**
       * 保存租户配置。
       */
        saveTenantConfig(tenantId, null, config);
    }

    /**
     * 保存租户级模块首页配置。
     *
     * @param tenantId 租户 ID
     * @param moduleCode 模块编码
     * @param config 首页配置
     */
    void saveTenantConfig(Long tenantId, String moduleCode, PersonalHomepageConfig config);

    /**
     * 读取公共级个人首页配置。
     */
    default PersonalHomepageConfig getPublicConfig() {
        /**
         * 获取public配置。
         *
         * @return 处理结果
         */
        return getPublicConfig(null);
    }

    /**
     * 读取公共级模块首页配置。
     *
     * @param moduleCode 模块编码
     * @return 首页配置
     */
    PersonalHomepageConfig getPublicConfig(String moduleCode);

    /**
     * 保存公共级个人首页配置。
     */
    default void savePublicConfig(PersonalHomepageConfig config) {
      /**
       * 保存public配置。
       */
        savePublicConfig(null, config);
    }

    /**
     * 保存公共级模块首页配置。
     *
     * @param moduleCode 模块编码
     * @param config 首页配置
     */
    void savePublicConfig(String moduleCode, PersonalHomepageConfig config);
}

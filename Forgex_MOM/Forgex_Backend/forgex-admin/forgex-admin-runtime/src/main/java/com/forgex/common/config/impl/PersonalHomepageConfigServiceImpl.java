package com.forgex.common.config.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.config.ConfigService;
import com.forgex.common.config.PersonalHomepageConfigService;
import com.forgex.common.domain.config.PersonalHomepageConfig;
import com.forgex.common.domain.entity.SysUserStyleConfig;
import com.forgex.common.mapper.SysUserStyleConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 个人首页配置服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Service
@DS("common")
@RequiredArgsConstructor
public class PersonalHomepageConfigServiceImpl implements PersonalHomepageConfigService {

    private static final String USER_CONFIG_KEY = "personal.homepage";
    private static final String DEFAULT_CONFIG_KEY = "personal.homepage.default";
    private static final String MODULE_USER_CONFIG_KEY_PREFIX = "module.homepage.";
    private static final String MODULE_DEFAULT_CONFIG_KEY_PREFIX = "module.homepage.default.";

    private final ConfigService configService;
    private final SysUserStyleConfigMapper userStyleConfigMapper;

    /**
     * 获取effective配置。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param moduleCode 模块编码
     * @return 处理结果
     */
    @Override
    public PersonalHomepageConfig getEffectiveConfig(Long userId, Long tenantId, String moduleCode) {
        PersonalHomepageConfig userConfig = getUserConfig(userId, tenantId, moduleCode);
        if (userConfig != null) {
            return normalize(userConfig, moduleCode);
        }

        PersonalHomepageConfig tenantConfig = getTenantConfig(tenantId, moduleCode);
        if (tenantConfig != null) {
            return normalize(tenantConfig, moduleCode);
        }

        PersonalHomepageConfig publicConfig = getPublicConfig(moduleCode);
        if (publicConfig != null) {
            return normalize(publicConfig, moduleCode);
        }

        return defaultConfig(moduleCode);
    }

    /**
     * 获取用户配置。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param moduleCode 模块编码
     * @return 处理结果
     */
    @Override
    public PersonalHomepageConfig getUserConfig(Long userId, Long tenantId, String moduleCode) {
        if (userId == null || tenantId == null) {
            return null;
        }

        String configKey = userConfigKey(moduleCode);
        SysUserStyleConfig entity = userStyleConfigMapper.selectOne(new LambdaQueryWrapper<SysUserStyleConfig>()
                .eq(SysUserStyleConfig::getUserId, userId)
                .eq(SysUserStyleConfig::getTenantId, tenantId)
                .eq(SysUserStyleConfig::getConfigKey, configKey)
                .eq(SysUserStyleConfig::getDeleted, false)
                .last("limit 1"));
        return parse(entity == null ? null : entity.getConfigJson(), moduleCode);
    }

    /**
     * 保存用户配置。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param moduleCode 模块编码
     * @param config 配置对象
     */
    @Override
    public void saveUserConfig(Long userId, Long tenantId, String moduleCode, PersonalHomepageConfig config) {
        if (userId == null || tenantId == null || config == null) {
            return;
        }

        String configKey = userConfigKey(moduleCode);
        String json = JSONUtil.toJsonStr(normalize(config, moduleCode));
        SysUserStyleConfig existing = userStyleConfigMapper.selectOne(new LambdaQueryWrapper<SysUserStyleConfig>()
                .eq(SysUserStyleConfig::getUserId, userId)
                .eq(SysUserStyleConfig::getTenantId, tenantId)
                .eq(SysUserStyleConfig::getConfigKey, configKey)
                .eq(SysUserStyleConfig::getDeleted, false)
                .last("limit 1"));

        if (existing == null) {
            SysUserStyleConfig entity = new SysUserStyleConfig();
            entity.setUserId(userId);
            entity.setTenantId(tenantId);
            entity.setConfigKey(configKey);
            entity.setConfigJson(json);
            entity.setDeleted(false);
            userStyleConfigMapper.insert(entity);
            return;
        }

        existing.setConfigJson(json);
        userStyleConfigMapper.updateById(existing);
    }

    /**
     * 处理重置user配置。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param moduleCode 模块编码
     * @return 是否处理成功
     */
    @Override
    public boolean resetUserConfig(Long userId, Long tenantId, String moduleCode) {
        if (userId == null || tenantId == null) {
            return false;
        }

        String configKey = userConfigKey(moduleCode);
        SysUserStyleConfig existing = userStyleConfigMapper.selectOne(new LambdaQueryWrapper<SysUserStyleConfig>()
                .eq(SysUserStyleConfig::getUserId, userId)
                .eq(SysUserStyleConfig::getTenantId, tenantId)
                .eq(SysUserStyleConfig::getConfigKey, configKey)
                .eq(SysUserStyleConfig::getDeleted, false)
                .last("limit 1"));
        if (existing == null) {
            return true;
        }
        return userStyleConfigMapper.deleteById(existing.getId()) > 0;
    }

    /**
     * 获取租户配置。
     *
     * @param tenantId 租户 ID
     * @param moduleCode 模块编码
     * @return 处理结果
     */
    @Override
    public PersonalHomepageConfig getTenantConfig(Long tenantId, String moduleCode) {
        if (tenantId == null) {
            return getPublicConfig(moduleCode);
        }
        PersonalHomepageConfig tenantConfig = configService.getJson(tenantDefaultConfigKey(tenantId, moduleCode), PersonalHomepageConfig.class, null);
        if (tenantConfig != null) {
            return normalize(tenantConfig, moduleCode);
        }
        PersonalHomepageConfig legacyConfig = configService.getJson(defaultConfigKey(moduleCode), PersonalHomepageConfig.class, null);
        if (legacyConfig != null) {
            return normalize(legacyConfig, moduleCode);
        }
        return getPublicConfig(moduleCode);
    }

    /**
     * 保存租户配置。
     *
     * @param tenantId 租户 ID
     * @param moduleCode 模块编码
     * @param config 配置对象
     */
    @Override
    public void saveTenantConfig(Long tenantId, String moduleCode, PersonalHomepageConfig config) {
        if (tenantId == null || config == null) {
            return;
        }
        configService.setJson(tenantDefaultConfigKey(tenantId, moduleCode), normalize(config, moduleCode));
    }

    /**
     * 获取public配置。
     *
     * @param moduleCode 模块编码
     * @return 处理结果
     */
    @Override
    public PersonalHomepageConfig getPublicConfig(String moduleCode) {
        PersonalHomepageConfig config = configService.getGlobalJson(defaultConfigKey(moduleCode), PersonalHomepageConfig.class, null);
        return config == null ? defaultConfig(moduleCode) : normalize(config, moduleCode);
    }

    /**
     * 保存public配置。
     *
     * @param moduleCode 模块编码
     * @param config 配置对象
     */
    @Override
    public void savePublicConfig(String moduleCode, PersonalHomepageConfig config) {
        if (config == null) {
            return;
        }
        configService.setGlobalJson(defaultConfigKey(moduleCode), normalize(config, moduleCode));
    }

    private PersonalHomepageConfig parse(String json, String moduleCode) {
        if (!JSONUtil.isTypeJSON(json)) {
            return null;
        }
        try {
            return normalize(JSONUtil.toBean(json, PersonalHomepageConfig.class), moduleCode);
        } catch (Exception ex) {
            return null;
        }
    }

    private PersonalHomepageConfig normalize(PersonalHomepageConfig config, String moduleCode) {
        if (config == null) {
            return defaultConfig(moduleCode);
        }
        PersonalHomepageConfig defaults = defaultConfig(moduleCode);
        if (config.getLayout() == null) {
            config.setLayout(defaults.getLayout());
        }
        if (config.getWidgets() == null || config.getWidgets().isEmpty()) {
            config.setWidgets(defaults.getWidgets());
        }
        return config;
    }

    private PersonalHomepageConfig defaultConfig(String moduleCode) {
        return PersonalHomepageConfig.defaults(normalizeModuleCode(moduleCode));
    }

    private String userConfigKey(String moduleCode) {
        String normalizedModuleCode = normalizeModuleCode(moduleCode);
        if ("personal".equals(normalizedModuleCode)) {
            return USER_CONFIG_KEY;
        }
        return MODULE_USER_CONFIG_KEY_PREFIX + normalizedModuleCode;
    }

    private String defaultConfigKey(String moduleCode) {
        String normalizedModuleCode = normalizeModuleCode(moduleCode);
        if ("personal".equals(normalizedModuleCode)) {
            return DEFAULT_CONFIG_KEY;
        }
        return MODULE_DEFAULT_CONFIG_KEY_PREFIX + normalizedModuleCode;
    }

    private String tenantDefaultConfigKey(Long tenantId, String moduleCode) {
        return defaultConfigKey(moduleCode) + ".tenant." + tenantId;
    }

    private String normalizeModuleCode(String moduleCode) {
        if (!StringUtils.hasText(moduleCode) || "personal".equalsIgnoreCase(moduleCode.trim())) {
            return "personal";
        }
        String normalized = moduleCode.trim().toLowerCase();
        if ("system".equals(normalized)) {
            return "sys";
        }
        if ("workflow".equals(normalized)) {
            return "approval";
        }
        return normalized;
    }
}

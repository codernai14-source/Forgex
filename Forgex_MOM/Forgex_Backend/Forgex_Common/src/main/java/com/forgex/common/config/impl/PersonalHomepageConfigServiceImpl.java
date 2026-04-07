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

/**
 * 个人首页配置服务实现。
 */
@Service
@DS("common")
@RequiredArgsConstructor
public class PersonalHomepageConfigServiceImpl implements PersonalHomepageConfigService {

    private static final String USER_CONFIG_KEY = "personal.homepage";
    private static final String DEFAULT_CONFIG_KEY = "personal.homepage.default";

    private final ConfigService configService;
    private final SysUserStyleConfigMapper userStyleConfigMapper;

    @Override
    public PersonalHomepageConfig getEffectiveConfig(Long userId, Long tenantId) {
        PersonalHomepageConfig userConfig = getUserConfig(userId, tenantId);
        if (userConfig != null) {
            return normalize(userConfig);
        }

        PersonalHomepageConfig tenantConfig = getTenantConfig(tenantId);
        if (tenantConfig != null) {
            return normalize(tenantConfig);
        }

        PersonalHomepageConfig publicConfig = getPublicConfig();
        if (publicConfig != null) {
            return normalize(publicConfig);
        }

        return PersonalHomepageConfig.defaults();
    }

    @Override
    public PersonalHomepageConfig getUserConfig(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return null;
        }

        SysUserStyleConfig entity = userStyleConfigMapper.selectOne(new LambdaQueryWrapper<SysUserStyleConfig>()
                .eq(SysUserStyleConfig::getUserId, userId)
                .eq(SysUserStyleConfig::getTenantId, tenantId)
                .eq(SysUserStyleConfig::getConfigKey, USER_CONFIG_KEY)
                .eq(SysUserStyleConfig::getDeleted, false)
                .last("limit 1"));
        return parse(entity == null ? null : entity.getConfigJson());
    }

    @Override
    public void saveUserConfig(Long userId, Long tenantId, PersonalHomepageConfig config) {
        if (userId == null || tenantId == null || config == null) {
            return;
        }

        String json = JSONUtil.toJsonStr(normalize(config));
        SysUserStyleConfig existing = userStyleConfigMapper.selectOne(new LambdaQueryWrapper<SysUserStyleConfig>()
                .eq(SysUserStyleConfig::getUserId, userId)
                .eq(SysUserStyleConfig::getTenantId, tenantId)
                .eq(SysUserStyleConfig::getConfigKey, USER_CONFIG_KEY)
                .eq(SysUserStyleConfig::getDeleted, false)
                .last("limit 1"));

        if (existing == null) {
            SysUserStyleConfig entity = new SysUserStyleConfig();
            entity.setUserId(userId);
            entity.setTenantId(tenantId);
            entity.setConfigKey(USER_CONFIG_KEY);
            entity.setConfigJson(json);
            entity.setDeleted(false);
            userStyleConfigMapper.insert(entity);
            return;
        }

        existing.setConfigJson(json);
        userStyleConfigMapper.updateById(existing);
    }

    @Override
    public boolean resetUserConfig(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return false;
        }

        SysUserStyleConfig existing = userStyleConfigMapper.selectOne(new LambdaQueryWrapper<SysUserStyleConfig>()
                .eq(SysUserStyleConfig::getUserId, userId)
                .eq(SysUserStyleConfig::getTenantId, tenantId)
                .eq(SysUserStyleConfig::getConfigKey, USER_CONFIG_KEY)
                .eq(SysUserStyleConfig::getDeleted, false)
                .last("limit 1"));
        if (existing == null) {
            return true;
        }
        return userStyleConfigMapper.deleteById(existing.getId()) > 0;
    }

    @Override
    public PersonalHomepageConfig getTenantConfig(Long tenantId) {
        if (tenantId == null) {
            return getPublicConfig();
        }
        PersonalHomepageConfig tenantConfig = configService.getJson(DEFAULT_CONFIG_KEY, PersonalHomepageConfig.class, null);
        if (tenantConfig != null) {
            return normalize(tenantConfig);
        }
        return getPublicConfig();
    }

    @Override
    public void saveTenantConfig(Long tenantId, PersonalHomepageConfig config) {
        if (tenantId == null || config == null) {
            return;
        }
        configService.setJson(DEFAULT_CONFIG_KEY, normalize(config));
    }

    @Override
    public PersonalHomepageConfig getPublicConfig() {
        PersonalHomepageConfig config = configService.getGlobalJson(DEFAULT_CONFIG_KEY, PersonalHomepageConfig.class, null);
        return config == null ? PersonalHomepageConfig.defaults() : normalize(config);
    }

    @Override
    public void savePublicConfig(PersonalHomepageConfig config) {
        if (config == null) {
            return;
        }
        configService.setGlobalJson(DEFAULT_CONFIG_KEY, normalize(config));
    }

    private PersonalHomepageConfig parse(String json) {
        if (!JSONUtil.isTypeJSON(json)) {
            return null;
        }
        try {
            return normalize(JSONUtil.toBean(json, PersonalHomepageConfig.class));
        } catch (Exception ex) {
            return null;
        }
    }

    private PersonalHomepageConfig normalize(PersonalHomepageConfig config) {
        if (config == null) {
            return PersonalHomepageConfig.defaults();
        }
        PersonalHomepageConfig defaults = PersonalHomepageConfig.defaults();
        if (config.getLayout() == null) {
            config.setLayout(defaults.getLayout());
        }
        if (config.getWidgets() == null || config.getWidgets().isEmpty()) {
            config.setWidgets(defaults.getWidgets());
        }
        return config;
    }
}

/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.common.config.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.forgex.common.config.UserStyleConfigService;
import com.forgex.common.domain.config.GuidePreferenceConfig;
import com.forgex.common.domain.config.LayoutStyleConfig;
import com.forgex.common.domain.entity.SysUserStyleConfig;
import com.forgex.common.mapper.SysUserStyleConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * 用户页面样式配置服务实现。
 *
 * <p>通过 {@link SysUserStyleConfigMapper} 操作 {@code sys_user_style_config} 表，
 * 使用 JSON 序列化保存 {@link LayoutStyleConfig}。</p>
 *
 * @author Forgex
 * @version 1.0.0
 */
@Service
@DS("common")
public class UserStyleConfigServiceImpl implements UserStyleConfigService {

    private static final String LAYOUT_STYLE_KEY = "layout.style";

    private static final String GUIDE_PREFERENCE_KEY = "guide.preference";

    @Autowired
    private SysUserStyleConfigMapper mapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutStyleConfig getLayoutConfig(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return LayoutStyleConfig.defaults();
        }
        SysUserStyleConfig entity = getConfigEntity(userId, tenantId, LAYOUT_STYLE_KEY);
        if (entity == null || entity.getConfigJson() == null) {
            return LayoutStyleConfig.defaults();
        }
        try {
            return JSONUtil.toBean(entity.getConfigJson(), LayoutStyleConfig.class);
        } catch (Exception e) {
            return LayoutStyleConfig.defaults();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveLayoutConfig(Long userId, Long tenantId, LayoutStyleConfig config) {
        if (userId == null || tenantId == null || config == null) {
            return;
        }
        saveConfigJson(userId, tenantId, LAYOUT_STYLE_KEY, JSONUtil.toJsonStr(config));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuidePreferenceConfig getGuidePreference(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return GuidePreferenceConfig.defaults();
        }
        SysUserStyleConfig entity = getConfigEntity(userId, tenantId, GUIDE_PREFERENCE_KEY);
        if (entity == null || entity.getConfigJson() == null) {
            return GuidePreferenceConfig.defaults();
        }
        try {
            GuidePreferenceConfig config = JSONUtil.toBean(
                    entity.getConfigJson(),
                    new TypeReference<GuidePreferenceConfig>() {},
                    true
            );
            return normalizeGuidePreferenceConfig(config);
        } catch (Exception e) {
            return GuidePreferenceConfig.defaults();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveGuidePreference(Long userId, Long tenantId, GuidePreferenceConfig config) {
        if (userId == null || tenantId == null || config == null) {
            return;
        }
        GuidePreferenceConfig normalized = normalizeGuidePreferenceConfig(config);
        saveConfigJson(userId, tenantId, GUIDE_PREFERENCE_KEY, JSONUtil.toJsonStr(normalized));
    }

    /**
     * 查询指定 key 的用户样式配置实体。
     *
     * @param userId    用户ID
     * @param tenantId  租户ID
     * @param configKey 配置键
     * @return 配置实体
     */
    private SysUserStyleConfig getConfigEntity(Long userId, Long tenantId, String configKey) {
        LambdaQueryWrapper<SysUserStyleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserStyleConfig::getUserId, userId)
                .eq(SysUserStyleConfig::getTenantId, tenantId)
                .eq(SysUserStyleConfig::getConfigKey, configKey)
                .eq(SysUserStyleConfig::getDeleted, 0)
                .last("limit 1");
        return mapper.selectOne(wrapper);
    }

    /**
     * 保存指定 key 的 JSON 配置内容。
     *
     * @param userId    用户ID
     * @param tenantId  租户ID
     * @param configKey 配置键
     * @param configJson 配置 JSON
     */
    private void saveConfigJson(Long userId, Long tenantId, String configKey, String configJson) {
        SysUserStyleConfig existing = getConfigEntity(userId, tenantId, configKey);
        if (existing == null) {
            SysUserStyleConfig entity = new SysUserStyleConfig();
            entity.setUserId(userId);
            entity.setTenantId(tenantId);
            entity.setConfigKey(configKey);
            entity.setConfigJson(configJson);
            entity.setDeleted(false);
            mapper.insert(entity);
            return;
        }
        existing.setConfigJson(configJson);
        mapper.updateById(existing);
    }

    /**
     * 标准化引导偏好配置，保证空值场景也能返回稳定结构。
     *
     * @param config 原始配置
     * @return 标准化后的配置
     */
    private GuidePreferenceConfig normalizeGuidePreferenceConfig(GuidePreferenceConfig config) {
        GuidePreferenceConfig normalized = config == null ? GuidePreferenceConfig.defaults() : config;
        if (normalized.getBabyModeEnabled() == null) {
            normalized.setBabyModeEnabled(Boolean.FALSE);
        }
        if (normalized.getGuideStates() == null) {
            normalized.setGuideStates(new LinkedHashMap<>());
        }
        return normalized;
    }
}


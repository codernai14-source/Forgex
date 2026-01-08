package com.forgex.common.config.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.forgex.common.config.UserStyleConfigService;
import com.forgex.common.domain.config.LayoutStyleConfig;
import com.forgex.common.domain.entity.SysUserStyleConfig;
import com.forgex.common.mapper.SysUserStyleConfigMapper;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        LambdaQueryWrapper<SysUserStyleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserStyleConfig::getUserId, userId)
                .eq(SysUserStyleConfig::getTenantId, tenantId)
                .eq(SysUserStyleConfig::getConfigKey, "layout.style")
                .eq(SysUserStyleConfig::getDeleted, 0)
                .last("limit 1");
        SysUserStyleConfig entity = mapper.selectOne(wrapper);
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
        String json = JSONUtil.toJsonStr(config);
        LambdaQueryWrapper<SysUserStyleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserStyleConfig::getUserId, userId)
                .eq(SysUserStyleConfig::getTenantId, tenantId)
                .eq(SysUserStyleConfig::getConfigKey, "layout.style")
                .eq(SysUserStyleConfig::getDeleted, 0)
                .last("limit 1");
        SysUserStyleConfig existing = mapper.selectOne(wrapper);
        if (existing == null) {
            SysUserStyleConfig entity = new SysUserStyleConfig();
            entity.setUserId(userId);
            entity.setTenantId(tenantId);
            entity.setConfigKey("layout.style");
            entity.setConfigJson(json);
            entity.setDeleted(0);
            mapper.insert(entity);
        } else {
            existing.setConfigJson(json);
            mapper.updateById(existing);
        }
    }
}


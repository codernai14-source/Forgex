package com.forgex.common.config;

import com.forgex.common.domain.config.LayoutStyleConfig;

/**
 * 用户页面样式配置服务。
 *
 * <p>统一负责用户在各租户下的页面布局与样式配置读写。</p>
 *
 * @author Forgex
 * @version 1.0.0
 */
public interface UserStyleConfigService {

    /**
     * 读取用户在某租户下的布局样式配置。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 若未配置或解析失败，则返回默认配置
     */
    LayoutStyleConfig getLayoutConfig(Long userId, Long tenantId);

    /**
     * 保存用户在某租户下的布局样式配置。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @param config   布局样式配置对象
     */
    void saveLayoutConfig(Long userId, Long tenantId, LayoutStyleConfig config);
}


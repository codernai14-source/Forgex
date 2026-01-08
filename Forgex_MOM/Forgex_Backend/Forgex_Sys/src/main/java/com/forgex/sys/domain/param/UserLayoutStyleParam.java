package com.forgex.sys.domain.param;

import com.forgex.common.domain.config.LayoutStyleConfig;
import lombok.Data;

/**
 * 用户布局样式参数。
 *
 * <p>用于前端与后端之间传递用户布局样式配置相关参数。</p>
 *
 * <p>前端只需要传入用户账号与租户ID，后端根据账号解析真实用户ID。</p>
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
public class UserLayoutStyleParam {

    /** 用户账号 */
    private String account;

    /** 租户ID */
    private Long tenantId;

    /** 布局样式配置体（保存时使用） */
    private LayoutStyleConfig config;
}

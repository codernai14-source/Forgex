package com.forgex.auth.domain.dto;

import lombok.Data;

/**
 * 用户DTO（Auth模块）
 * <p>
 * 用于在认证模块中向前端返回当前登录用户的基础信息，
 * 包含账号、用户名、邮箱、手机号、头像、状态以及当前绑定的租户ID等字段。
 * </p>
 *
 * <p>该DTO与系统模块中的用户DTO解耦，避免跨模块依赖。</p>
 *
 * @author coder_nai
 * @version 1.0.0
 * @see com.forgex.auth.service.impl.AuthServiceImpl
 */
@Data
public class SysUserDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 账号
     */
    private String account;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 状态：false=禁用，true=启用
     */
    private Boolean status;

    /**
     * 当前选择的租户ID
     */
    private Long tenantId;
}


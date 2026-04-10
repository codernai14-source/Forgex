package com.forgex.auth.domain.dto;

import lombok.Data;

/**
 * 用户 DTO（Auth 模块）
 * <p>
 * 用于在认证模块中向前端返回当前登录用户的基础信息，
 * 包含账号、用户名、邮箱、手机号、头像、状态以及当前绑定的租户 ID 等字段。
 * </p>
 *
 * <p>该 DTO 与系统模块中的用户 DTO 解耦，避免跨模块依赖。</p>
 *
 * @author coder_nai
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.auth.service.impl.AuthServiceImpl
 * @see com.forgex.auth.controller.AuthController
 */
@Data
public class SysUserDTO {

    /**
     * 主键 ID（雪花算法生成）
     */
    private Long id;

    /**
     * 账号
     * <p>用于登录的唯一标识</p>
     */
    private String account;

    /**
     * 用户名
     * <p>用户的显示名称</p>
     */
    private String username;

    /**
     * 邮箱地址
     * <p>用户的邮箱地址</p>
     */
    private String email;

    /**
     * 手机号码
     * <p>用户的手机号码</p>
     */
    private String phone;

    /**
     * 头像 URL
     * <p>用户头像的图片地址</p>
     */
    private String avatar;

    /**
     * 用户状态
     * <p>
     * false：禁用<br>
     * true：启用
     * </p>
     */
    private Boolean status;

    /**
     * 当前选择的租户 ID
     * <p>用户当前登录的租户 ID</p>
     */
    private Long tenantId;
}

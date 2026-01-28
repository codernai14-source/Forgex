package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户角色绑定实体（用于权限计算的轻量映射）。
 * <p>
 * 映射表：{@code sys_user_role}。用于查询用户在租户下绑定的角色列表。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Data
@TableName("sys_user_role")
public class SysUserRolePerm {

    /**
     * 主键ID（不同初始化脚本可能存在/不存在，保留字段不影响查询）。
     */
    private Long id;

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 角色ID。
     */
    private Long roleId;

    /**
     * 租户ID。
     */
    private Long tenantId;
}


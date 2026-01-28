package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色菜单授权实体（用于权限计算的轻量映射）。
 * <p>
 * 映射表：{@code sys_role_menu}。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenuPerm {

    /**
     * 主键ID。
     */
    private Long id;

    /**
     * 租户ID。
     */
    private Long tenantId;

    /**
     * 角色ID。
     */
    private Long roleId;

    /**
     * 菜单ID。
     */
    private Long menuId;
}


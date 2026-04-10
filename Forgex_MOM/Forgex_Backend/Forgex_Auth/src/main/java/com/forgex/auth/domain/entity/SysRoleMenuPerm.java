package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色菜单授权实体（用于权限计算的轻量映射）
 * <p>
 * 映射表：{@code sys_role_menu}，用于存储角色与菜单的关联关系。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.auth.security.StpInterfaceImpl
 * @see com.forgex.auth.mapper.SysRoleMenuPermMapper
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenuPerm {

    /**
     * 主键 ID（雪花算法生成）
     */
    private Long id;

    /**
     * 租户 ID
     * <p>用于多租户隔离，标识该授权记录所属租户</p>
     */
    private Long tenantId;

    /**
     * 角色 ID
     * <p>关联到角色表的主键</p>
     */
    private Long roleId;

    /**
     * 菜单 ID
     * <p>关联到菜单表的主键</p>
     */
    private Long menuId;
}

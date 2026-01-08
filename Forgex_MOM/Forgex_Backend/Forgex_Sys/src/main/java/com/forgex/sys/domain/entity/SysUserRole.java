package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-角色绑定实体。
 * <p>
 * 映射表：{@code sys_user_role}。字段：
 * - {@code userId} 用户ID；
 * - {@code roleId} 角色ID；
 * - {@code tenantId} 租户ID。
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {
    /** 主键ID */
    @TableId
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 角色ID */
    private Long roleId;
    /** 租户ID */
    private Long tenantId;
}

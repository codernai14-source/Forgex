package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 管理库角色实体（含租户维度）。
 * <p>
 * 映射表：{@code sys_role}。字段：
 * - {@code roleName} 角色名称；
 * - {@code roleKey} 角色键；
 * - {@code status} 状态；
 * - {@code tenantId} 租户ID。
 */
@Data
@TableName("sys_role")
public class SysRole extends BaseEntity {
    /** 角色名称 */
    private String roleName;
    /** 角色键 */
    private String roleKey;
    /** 状态 */
    private Integer status;
}

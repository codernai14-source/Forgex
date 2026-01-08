package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 租户忽略规则实体。
 * <p>
 * 映射表：{@code sys_tenant_ignore}。用于记录在特定范围/匹配器下忽略租户过滤的规则。
 * 字段：
 * - {@code scope} 作用范围标识；
 * - {@code matcher} 匹配器表达式；
 * - {@code enabled} 是否启用；
 * - {@code remark} 备注；
 * - {@code tenantId} 绑定租户ID（非持久化）。
 */
@Data
@TableName("sys_tenant_ignore")
public class SysTenantIgnore extends BaseEntity {
    /** 作用范围标识 */
    private String scope;
    /** 匹配器表达式 */
    private String matcher;
    /** 是否启用 */
    private Integer enabled;
    /** 备注 */
    private String remark;
    /** 绑定租户ID（非持久化） */
    @TableField(exist = false)
    private Long tenantId;
}

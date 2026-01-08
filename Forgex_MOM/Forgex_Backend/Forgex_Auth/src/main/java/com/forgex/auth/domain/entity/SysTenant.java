package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 租户实体（持久化对象）
 * 对应数据库表：sys_tenant
 */
@Data
@TableName("sys_tenant")
public class SysTenant extends BaseEntity {
    /** 租户名称 */
    private String tenantName;
    /** 描述 */
    private String description;
    /** 状态：1启用 0禁用 */
    private Integer status;
    /** 编码 */
    private String tenantCode;
    /** Logo */
    private String logo;
    /** 租户ID */
    @TableField(exist = false)
    private Long tenantId;
}

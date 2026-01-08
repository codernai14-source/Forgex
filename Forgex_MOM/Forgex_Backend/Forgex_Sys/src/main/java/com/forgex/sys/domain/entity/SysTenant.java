package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 管理库租户实体。
 * <p>
 * 映射表：{@code sys_tenant}。用于描述租户基本信息与展示属性。
 * 字段：
 * - {@code tenantName} 租户名称；
 * - {@code description} 描述；
 * - {@code tenantCode} 唯一编码；
 * - {@code logo} 展示 Logo（URL）。
 * <p>
 * 可扩展性：可按需增加联系人、到期时间、配额等业务字段。
 */
@Data
@TableName("sys_tenant")
public class SysTenant extends BaseEntity {
    /** 租户名称 */
    private String tenantName;
    /** 描述 */
    private String description;
    /** 唯一编码 */
    private String tenantCode;
    /** Logo URL */
    private String logo;
}

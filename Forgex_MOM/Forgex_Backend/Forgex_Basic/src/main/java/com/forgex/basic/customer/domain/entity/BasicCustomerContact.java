package com.forgex.basic.customer.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户联系人实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_customer_contact")
public class BasicCustomerContact extends BaseEntity {
    @TableField("customer_id")
    private Long customerId;
    @TableField("contact_name")
    private String contactName;
    @TableField("contact_position")
    private String contactPosition;
    @TableField("contact_phone")
    private String contactPhone;
}

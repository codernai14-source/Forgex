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
    /**
     * 客户 ID。
     */
    @TableField("customer_id")
    private Long customerId;
    /**
     * 联系人名称。
     */
    @TableField("contact_name")
    private String contactName;
    /**
     * 联系人position。
     */
    @TableField("contact_position")
    private String contactPosition;
    /**
     * 联系人手机号。
     */
    @TableField("contact_phone")
    private String contactPhone;
}

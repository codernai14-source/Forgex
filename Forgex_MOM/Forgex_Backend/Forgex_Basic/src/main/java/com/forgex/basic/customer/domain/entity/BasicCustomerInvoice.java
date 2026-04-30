package com.forgex.basic.customer.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户发票抬头实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_customer_invoice")
public class BasicCustomerInvoice extends BaseEntity {
    @TableField("customer_id")
    private Long customerId;
    @TableField("invoice_full_name")
    private String invoiceFullName;
    @TableField("tax_number")
    private String taxNumber;
    @TableField("registered_address")
    private String registeredAddress;
    @TableField("registered_phone")
    private String registeredPhone;
    @TableField("bank_name")
    private String bankName;
    @TableField("bank_account")
    private String bankAccount;
    @TableField("invoice_required")
    private Boolean invoiceRequired;
}

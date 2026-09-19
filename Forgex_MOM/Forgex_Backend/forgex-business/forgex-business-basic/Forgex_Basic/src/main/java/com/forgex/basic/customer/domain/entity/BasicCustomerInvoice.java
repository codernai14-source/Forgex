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
    /**
     * 客户 ID。
     */
    @TableField("customer_id")
    private Long customerId;
    /**
     * 开票全称名称。
     */
    @TableField("invoice_full_name")
    private String invoiceFullName;
    /**
     * 税号。
     */
    @TableField("tax_number")
    private String taxNumber;
    /**
     * 注册地址。
     */
    @TableField("registered_address")
    private String registeredAddress;
    /**
     * 注册电话。
     */
    @TableField("registered_phone")
    private String registeredPhone;
    /**
     * 开户银行。
     */
    @TableField("bank_name")
    private String bankName;
    /**
     * 银行账号。
     */
    @TableField("bank_account")
    private String bankAccount;
    /**
     * 开票required。
     */
    @TableField("invoice_required")
    private Boolean invoiceRequired;
}

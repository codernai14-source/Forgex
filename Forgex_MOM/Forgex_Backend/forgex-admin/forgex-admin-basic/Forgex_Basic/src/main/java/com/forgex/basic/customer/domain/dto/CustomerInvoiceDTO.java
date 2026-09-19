package com.forgex.basic.customer.domain.dto;

import lombok.Data;

/**
 * 客户开票数据传输对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class CustomerInvoiceDTO {
    /**
     * 主键 ID。
     */
    private Long id;
    /**
     * 客户 ID。
     */
    private Long customerId;
    /**
     * 开票全称名称。
     */
    private String invoiceFullName;
    /**
     * 税号。
     */
    private String taxNumber;
    /**
     * 注册地址。
     */
    private String registeredAddress;
    /**
     * 注册电话。
     */
    private String registeredPhone;
    /**
     * 开户银行。
     */
    private String bankName;
    /**
     * 银行账号。
     */
    private String bankAccount;
    /**
     * 开票required。
     */
    private Boolean invoiceRequired;
}

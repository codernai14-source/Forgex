package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 客户开票同步 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Data
public class CustomerInvoiceSyncDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long customerId;

    private String invoiceFullName;

    private String taxNumber;

    private String registeredAddress;

    private String registeredPhone;

    private String bankName;

    private String bankAccount;

    private Boolean invoiceRequired;
}

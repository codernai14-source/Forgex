package com.forgex.basic.customer.domain.dto;

import lombok.Data;

@Data
public class CustomerInvoiceDTO {
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

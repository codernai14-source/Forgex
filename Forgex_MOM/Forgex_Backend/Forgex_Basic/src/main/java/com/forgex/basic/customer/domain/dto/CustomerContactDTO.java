package com.forgex.basic.customer.domain.dto;

import lombok.Data;

@Data
public class CustomerContactDTO {
    private Long id;
    private Long customerId;
    private String contactName;
    private String contactPosition;
    private String contactPhone;
}

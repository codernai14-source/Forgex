package com.forgex.basic.customer.domain.param;

import lombok.Data;

import java.util.List;

@Data
public class CustomerApprovalStartParam {
    private Long customerId;
    private List<Long> selectedApprovers;
}

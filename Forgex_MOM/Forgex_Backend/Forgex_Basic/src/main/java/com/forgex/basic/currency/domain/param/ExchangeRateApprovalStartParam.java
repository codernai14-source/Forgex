package com.forgex.basic.currency.domain.param;

import lombok.Data;

import java.util.List;

@Data
public class ExchangeRateApprovalStartParam {
    private Long rateId;
    private List<Long> selectedApprovers;
}

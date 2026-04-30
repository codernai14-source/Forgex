package com.forgex.basic.currency.domain.param;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExchangeRateCurrentParam {
    private String sourceCurrencyCode;
    private String targetCurrencyCode;
    private String rateTypeCode;
    private Long orgId;
    private LocalDate rateDate;
}

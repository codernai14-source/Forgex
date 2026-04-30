package com.forgex.basic.currency.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExchangeRatePageParam extends BaseGetParam {
    private String sourceCurrencyCode;
    private String targetCurrencyCode;
    private String rateTypeCode;
    private Integer approveStatus;
    private Long orgId;
    private LocalDate effectiveDate;
}

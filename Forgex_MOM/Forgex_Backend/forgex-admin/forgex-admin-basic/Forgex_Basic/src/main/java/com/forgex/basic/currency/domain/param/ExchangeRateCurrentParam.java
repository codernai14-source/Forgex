package com.forgex.basic.currency.domain.param;

import lombok.Data;

import java.time.LocalDate;

/**
 * 汇率当前请求参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class ExchangeRateCurrentParam {
    /**
     * 来源币种编码。
     */
    private String sourceCurrencyCode;
    /**
     * 目标币种编码。
     */
    private String targetCurrencyCode;
    /**
     * 汇率类型编码。
     */
    private String rateTypeCode;
    /**
     * org ID。
     */
    private Long orgId;
    /**
     * 汇率日期。
     */
    private LocalDate rateDate;
}

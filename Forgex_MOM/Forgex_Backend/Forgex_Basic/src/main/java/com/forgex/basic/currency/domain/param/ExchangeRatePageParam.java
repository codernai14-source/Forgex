package com.forgex.basic.currency.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 汇率分页请求参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExchangeRatePageParam extends BaseGetParam {
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
     * 审批状态。
     */
    private Integer approveStatus;
    /**
     * org ID。
     */
    private Long orgId;
    /**
     * 生效日期。
     */
    private LocalDate effectiveDate;
}

package com.forgex.basic.currency.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CurrencyPageParam extends BaseGetParam {
    private String currencyCode;
    private String currencyNameCn;
    private Boolean isBaseCurrency;
    private Integer status;
}

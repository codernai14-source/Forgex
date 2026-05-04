package com.forgex.basic.currency.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 币种分页请求参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CurrencyPageParam extends BaseGetParam {
    /**
     * 币种字母编码。
     */
    private String currencyCode;
    /**
     * 币种中文名称。
     */
    private String currencyNameCn;
    /**
     * 是否本位币。
     */
    private Boolean isBaseCurrency;
    /**
     * 状态。
     */
    private Integer status;
}

package com.forgex.basic.currency.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RateTypePageParam extends BaseGetParam {
    private String rateTypeCode;
    private String rateTypeName;
    private Boolean isDefault;
    private Integer status;
}

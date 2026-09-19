package com.forgex.basic.currency.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 汇率类型分页请求参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RateTypePageParam extends BaseGetParam {
    /**
     * 汇率类型编码。
     */
    private String rateTypeCode;
    /**
     * 汇率类型名称。
     */
    private String rateTypeName;
    /**
     * 是否默认。
     */
    private Boolean isDefault;
    /**
     * 状态。
     */
    private Integer status;
}

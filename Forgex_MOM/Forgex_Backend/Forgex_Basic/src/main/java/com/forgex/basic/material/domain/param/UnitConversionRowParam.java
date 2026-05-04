package com.forgex.basic.material.domain.param;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 计量单位换算关系行参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
public class UnitConversionRowParam {

    /**
     * 换算关系 ID
     */
    private Long id;

    /**
     * 目标计量单位 ID
     */
    private Long targetUnitId;

    /**
     * 换算后数值，表示 1 源单位 = conversionValue 目标单位
     */
    private BigDecimal conversionValue;
}

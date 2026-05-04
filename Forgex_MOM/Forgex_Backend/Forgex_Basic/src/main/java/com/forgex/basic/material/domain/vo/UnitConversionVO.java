package com.forgex.basic.material.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 计量单位换算关系返回对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
public class UnitConversionVO {

    private Long id;

    private Long unitId;

    private String unitCode;

    private String unitName;

    private Long targetUnitId;

    private String targetUnitCode;

    private String targetUnitName;

    private BigDecimal conversionValue;
}

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

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 单位 ID。
     */
    private Long unitId;

    /**
     * 单位编码。
     */
    private String unitCode;

    /**
     * 单位名称。
     */
    private String unitName;

    /**
     * 目标单位 ID。
     */
    private Long targetUnitId;

    /**
     * 目标单位编码。
     */
    private String targetUnitCode;

    /**
     * 目标单位名称。
     */
    private String targetUnitName;

    /**
     * 换算值。
     */
    private BigDecimal conversionValue;
}

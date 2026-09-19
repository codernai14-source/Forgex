package com.forgex.basic.material.domain.param;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 计量单位换算关系保存参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
public class UnitConversionSaveParam {

    /**
     * 源计量单位 ID
     */
    private Long unitId;

    /**
     * 换算关系明细
     */
    private List<UnitConversionRowParam> conversions = new ArrayList<>();
}

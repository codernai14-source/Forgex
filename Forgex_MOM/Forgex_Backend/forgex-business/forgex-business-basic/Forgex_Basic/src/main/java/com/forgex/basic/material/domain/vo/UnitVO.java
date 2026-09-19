package com.forgex.basic.material.domain.vo;

import com.forgex.basic.material.domain.entity.BasicUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计量单位主数据返回对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UnitVO extends BasicUnit {

    /**
     * 计量单位类型编码
     */
    private String unitTypeCode;

    /**
     * 计量单位类型名称
     */
    private String unitTypeName;
}

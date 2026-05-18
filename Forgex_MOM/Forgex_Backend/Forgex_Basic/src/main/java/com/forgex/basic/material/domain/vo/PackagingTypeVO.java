package com.forgex.basic.material.domain.vo;

import com.forgex.basic.material.domain.entity.BasicPackagingType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 包装规格列表视图对象。
 * <p>
 * 在包装规格主数据基础上补充计量单位名称，供公共表格组件直接展示。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PackagingTypeVO extends BasicPackagingType {

    /**
     * 尺寸单位名称。
     */
    private String sizeUnitName;

    /**
     * 包装尺寸展示文本。
     */
    private String size;

    /**
     * 容积单位名称。
     */
    private String volumeUnitName;

    /**
     * 包装容积展示文本。
     */
    private String volume;

    /**
     * 重量单位名称。
     */
    private String weightUnitName;

    /**
     * 包装重量展示文本。
     */
    private String weight;
}

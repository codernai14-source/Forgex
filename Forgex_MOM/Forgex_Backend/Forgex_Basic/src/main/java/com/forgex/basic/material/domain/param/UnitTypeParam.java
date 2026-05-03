package com.forgex.basic.material.domain.param;

import lombok.Data;

/**
 * 计量单位类型保存参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
public class UnitTypeParam {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 类型编码
     */
    private String unitTypeCode;

    /**
     * 类型名称
     */
    private String unitTypeName;

    /**
     * 父级 ID
     */
    private Long parentId;
}

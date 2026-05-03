package com.forgex.basic.material.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计量单位分页查询参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UnitPageParam extends BaseGetParam {

    /**
     * 计量单位类型 ID
     */
    private Long unitTypeId;

    /**
     * 计量单位编码
     */
    private String unitCode;

    /**
     * 计量单位名称
     */
    private String unitName;
}

package com.forgex.basic.material.domain.param;

import lombok.Data;

/**
 * 物料三槽包装规格保存参数。
 * <p>
 * 三个槽位固定为小包装、中包装和大包装；空值表示清除对应槽位绑定。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-16
 */
@Data
public class MaterialPackagingSaveParam {

    /**
     * 物料 ID。
     */
    private Long materialId;

    /**
     * 小包装规格 ID。
     */
    private Long smallPackagingTypeId;

    /**
     * 中包装规格 ID。
     */
    private Long mediumPackagingTypeId;

    /**
     * 大包装规格 ID。
     */
    private Long largePackagingTypeId;
}

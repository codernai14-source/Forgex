package com.forgex.basic.material.domain.param;

import lombok.Data;

/**
 * 单槽包装规格绑定参数。
 * <p>
 * 用于从包装规格页维护物料与当前包装规格的某一个槽位关系。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-16
 */
@Data
public class MaterialPackagingSlotSaveParam {

    /**
     * 物料 ID。
     */
    private Long materialId;

    /**
     * 包装规格 ID。
     */
    private Long packagingTypeId;

    /**
     * 包装槽位：SMALL=小包装，MEDIUM=中包装，LARGE=大包装。
     */
    private String packagingSlot;
}

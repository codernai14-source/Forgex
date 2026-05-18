package com.forgex.basic.material.domain.vo;

import lombok.Data;

/**
 * 物料包装规格关联视图对象。
 * <p>
 * 聚合物料、包装规格和槽位信息，供包装规格页和物料页的关联弹窗展示。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-16
 */
@Data
public class MaterialPackagingRelationVO {

    /**
     * 关联记录 ID。
     */
    private Long id;

    /**
     * 物料 ID。
     */
    private Long materialId;

    /**
     * 物料编码。
     */
    private String materialCode;

    /**
     * 物料名称。
     */
    private String materialName;

    /**
     * 物料类型。
     */
    private String materialType;

    /**
     * 包装规格 ID。
     */
    private Long packagingTypeId;

    /**
     * 包装规格编码。
     */
    private String packagingCode;

    /**
     * 包装规格名称。
     */
    private String packagingName;

    /**
     * 包装规格类型。
     */
    private String packagingSpecType;

    /**
     * 包装槽位：SMALL=小包装，MEDIUM=中包装，LARGE=大包装。
     */
    private String packagingSlot;

    /**
     * 包装槽位名称。
     */
    private String packagingSlotName;
}

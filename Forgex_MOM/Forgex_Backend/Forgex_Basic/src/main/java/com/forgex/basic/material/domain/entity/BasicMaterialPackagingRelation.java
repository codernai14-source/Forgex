package com.forgex.basic.material.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物料包装规格关联实体类。
 * <p>
 * 对应数据库表：basic_material_packaging_relation，用于保存物料与包装规格在小包装、中包装、大包装三个固定槽位上的绑定关系。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-16
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_material_packaging_relation")
public class BasicMaterialPackagingRelation extends BaseEntity {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物料 ID，关联 basic_material。
     */
    @TableField("material_id")
    private Long materialId;

    /**
     * 包装规格 ID，关联 basic_packaging_type。
     */
    @TableField("packaging_type_id")
    private Long packagingTypeId;

    /**
     * 包装槽位：SMALL=小包装，MEDIUM=中包装，LARGE=大包装。
     */
    @TableField("packaging_slot")
    private String packagingSlot;
}

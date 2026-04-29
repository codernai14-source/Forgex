
package com.forgex.basic.material.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 包装方式实体类
 * <p>
 * 对应数据库表：basic_packaging_type
 * 用于存储物料包装方式信息，支持多种包装规格和材质
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_packaging_type")
public class BasicPackagingType extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 包装方式编码（租户内唯一）
     */
    @TableField("packaging_code")
    private String packagingCode;

    /**
     * 包装方式名称
     */
    @TableField("packaging_name")
    private String packagingName;

    /**
     * 包装材料（纸箱/木箱/托盘/铁桶等）
     */
    @TableField("packaging_material")
    private String packagingMaterial;

    /**
     * 长度（mm）
     */
    @TableField("length_mm")
    private BigDecimal lengthMm;

    /**
     * 宽度（mm）
     */
    @TableField("width_mm")
    private BigDecimal widthMm;

    /**
     * 高度（mm）
     */
    @TableField("height_mm")
    private BigDecimal heightMm;

    /**
     * 包装自重（kg）
     */
    @TableField("weight_kg")
    private BigDecimal weightKg;

    /**
     * 最大承重（kg）
     */
    @TableField("max_load_kg")
    private BigDecimal maxLoadKg;

    /**
     * 单位成本
     */
    @TableField("unit_cost")
    private BigDecimal unitCost;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

}

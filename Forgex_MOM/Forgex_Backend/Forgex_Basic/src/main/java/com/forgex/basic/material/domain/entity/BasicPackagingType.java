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
 * 包装规格实体类。
 * <p>
 * 对应数据库表：basic_packaging_type，用于维护箱、桶、卷、盒、袋等包装规格主数据。
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
     * 主键 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 包装规格编码，租户内唯一。
     */
    @TableField("packaging_code")
    private String packagingCode;

    /**
     * 包装规格名称。
     */
    @TableField("packaging_name")
    private String packagingName;

    /**
     * 包装规格类型，取值来自字典 packaging_spec_type。
     */
    @TableField("packaging_spec_type")
    private String packagingSpecType;

    /**
     * 长度。
     */
    @TableField("length_value")
    private BigDecimal lengthValue;

    /**
     * 宽度。
     */
    @TableField("width_value")
    private BigDecimal widthValue;

    /**
     * 高度。
     */
    @TableField("height_value")
    private BigDecimal heightValue;

    /**
     * 尺寸单位 ID，关联 basic_unit。
     */
    @TableField("size_unit_id")
    private Long sizeUnitId;

    /**
     * 包装容积。
     */
    @TableField("volume_value")
    private BigDecimal volumeValue;

    /**
     * 容积单位 ID，关联 basic_unit。
     */
    @TableField("volume_unit_id")
    private Long volumeUnitId;

    /**
     * 重量。
     */
    @TableField("weight_value")
    private BigDecimal weightValue;

    /**
     * 重量单位 ID，关联 basic_unit。
     */
    @TableField("weight_unit_id")
    private Long weightUnitId;

    /**
     * 状态：0-禁用，1-启用。
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序号。
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 备注。
     */
    @TableField("remark")
    private String remark;
}

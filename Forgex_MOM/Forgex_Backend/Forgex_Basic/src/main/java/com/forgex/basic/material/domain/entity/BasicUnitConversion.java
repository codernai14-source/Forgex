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
 * 计量单位换算关系实体。
 * <p>
 * 表示 1 个源计量单位换算为目标计量单位后的数值。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_unit_conversion")
public class BasicUnitConversion extends BaseEntity {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 源计量单位 ID
     */
    @TableField("unit_id")
    private Long unitId;

    /**
     * 目标计量单位 ID
     */
    @TableField("target_unit_id")
    private Long targetUnitId;

    /**
     * 换算值。
     */
    @TableField("conversion_value")
    private BigDecimal conversionValue;
}

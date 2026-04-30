
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
 * 计量单位实体类
 * <p>
 * 对应数据库表：basic_unit
 * 用于存储物料计量单位信息，支持多分类和换算比率
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_unit")
public class BasicUnit extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 单位编码（租户内唯一）
     */
    @TableField("unit_code")
    private String unitCode;

    /**
     * 单位名称
     */
    @TableField("unit_name")
    private String unitName;

    /**
     * 单位符号
     */
    @TableField("unit_symbol")
    private String unitSymbol;

    /**
     * 单位分类（长度/重量/体积/数量等）
     */
    @TableField("unit_category")
    private String unitCategory;

    /**
     * 换算比率（相对于标准单位）
     */
    @TableField("conversion_rate")
    private BigDecimal conversionRate;

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

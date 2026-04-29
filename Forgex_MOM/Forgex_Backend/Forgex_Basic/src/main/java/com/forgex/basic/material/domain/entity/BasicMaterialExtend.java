package com.forgex.basic.material.domain.entity;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 物料扩展信息实体类
 * <p>
 * 对应数据库表：basic_material_extend
 * 用于存储物料按模块划分的扩展信息，支持动态配置
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_material_extend")
public class BasicMaterialExtend extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物料 ID（关联 basic_material 表）
     */
    @TableField("material_id")
    private Long materialId;

    /**
     * 模块编码
     * PURCHASE=采购，INVENTORY=库存，PRODUCTION=生产，SALES=销售
     */
    @TableField("module")
    private String module;

    /**
     * 扩展信息 JSON 数据
     * 存储格式：{"field1": "value1", "field2": "value2", ...}
     */
    @TableField("extend_json")
    private String extendJson;

    /**
     * 最低库存
     */
    @TableField("min_stock")
    private BigDecimal minStock;

    /**
     * 最高库存
     */
    @TableField("max_stock")
    private BigDecimal maxStock;

    /**
     * 安全库存
     */
    @TableField("safety_stock")
    private BigDecimal safetyStock;

    /**
     * 有效周期值
     */
    @TableField("valid_period_value")
    private Integer validPeriodValue;

    /**
     * 有效周期单位（YEAR=年, MONTH=月, DAY=日, HOUR=时）
     */
    @TableField("valid_period_unit")
    private String validPeriodUnit;

    /**
     * 呆滞周期值
     */
    @TableField("stagnant_period_value")
    private Integer stagnantPeriodValue;

    /**
     * 呆滞周期单位（YEAR=年, MONTH=月, DAY=日, HOUR=时）
     */
    @TableField("stagnant_period_unit")
    private String stagnantPeriodUnit;

    /**
     * 包装方式ID（关联包装方式管理表）
     */
    @TableField("packaging_type_id")
    private Long packagingTypeId;

}

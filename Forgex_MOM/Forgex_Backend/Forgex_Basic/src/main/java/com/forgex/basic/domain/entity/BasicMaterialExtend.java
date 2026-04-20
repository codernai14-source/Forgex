package com.forgex.basic.domain.entity;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

}

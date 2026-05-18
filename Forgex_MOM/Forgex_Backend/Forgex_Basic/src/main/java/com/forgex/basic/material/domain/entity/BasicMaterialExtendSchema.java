package com.forgex.basic.material.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物料附属字段结构实体。
 * <p>
 * 对应数据库表：basic_material_extend_schema，用于按模块和物料类型保存完整字段 JSON 结构。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-12
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_material_extend_schema")
public class BasicMaterialExtendSchema extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模块编码。
     */
    @TableField("module")
    private String module;

    /**
     * 物料类型。
     */
    @TableField("material_type")
    private String materialType;

    /**
     * 完整字段结构 JSON。
     */
    @TableField("schema_json")
    private String schemaJson;

    /**
     * 结构版本号。
     */
    @TableField("version")
    private Integer version;

    /**
     * 状态（0=禁用，1=启用）。
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序号。
     */
    @TableField("order_num")
    private Integer orderNum;

    /**
     * 备注。
     */
    @TableField("remark")
    private String remark;
}

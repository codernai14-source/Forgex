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
 * 物料扩展字段配置表实体类
 * <p>
 * 对应数据库表：basic_material_extend_config
 * 用于动态配置物料在不同业务模块下的扩展字段，支持多租户隔离
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_material_extend_config")
public class BasicMaterialExtendConfig extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模块编码
     * PURCHASE=采购，INVENTORY=库存，PRODUCTION=生产，SALES=销售
     */
    @TableField("module")
    private String module;

    /**
     * 字段名称（英文标识）
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 字段标签（中文显示）
     */
    @TableField("field_label")
    private String fieldLabel;

    /**
     * 字段类型
     * STRING=字符串，NUMBER=数字，DATE=日期，BOOLEAN=布尔，SELECT=下拉框，MULTI_SELECT=多选
     */
    @TableField("field_type")
    private String fieldType;

    /**
     * 字段选项（JSON 格式，用于下拉框等类型）
     */
    @TableField("field_options")
    private String fieldOptions;

    /**
     * 是否必填（0=否，1=是）
     */
    @TableField("required")
    private Integer required;

    /**
     * 校验规则（正则表达式或自定义规则）
     */
    @TableField("validation_rule")
    private String validationRule;

    /**
     * 默认值
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 排序号
     */
    @TableField("order_num")
    private Integer orderNum;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 状态（0=禁用，1=启用）
     */
    @TableField("status")
    private Integer status;
}

package com.forgex.basic.label.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签打印参数配置表实体类
 * <p>
 * 对应数据库表：label_print_config
 * 用于存储标签打印相关的参数配置，支持全局、工厂、模板类型等多维度配置
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("label_print_config")
public class LabelPrintConfig extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 配置键
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 配置值
     */
    @TableField("config_value")
    private String configValue;

    /**
     * 配置类型
     * STRING=字符串，NUMBER=数字，BOOLEAN=布尔，JSON=JSON 对象
     */
    @TableField("config_type")
    private String configType;

    /**
     * 配置说明
     */
    @TableField("description")
    private String description;

    /**
     * 作用范围
     * GLOBAL=全局，FACTORY=工厂，TEMPLATE_TYPE=模板类型
     */
    @TableField("scope_type")
    private String scopeType;

    /**
     * 作用范围值（工厂 ID 或模板类型）
     */
    @TableField("scope_value")
    private String scopeValue;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;
}

package com.forgex.basic.label.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签模板绑定表实体类
 * <p>
 * 对应数据库表：label_template_binding
 * 用于存储模板与业务对象（物料、供应商、客户）的绑定关系
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("label_template_binding")
public class LabelBinding extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模板 ID
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 绑定类型
     * MATERIAL=按物料，SUPPLIER=按供应商，CUSTOMER=按客户
     */
    @TableField("binding_type")
    private String bindingType;

    /**
     * 绑定值（物料编码/供应商编码/客户编码）
     */
    @TableField("binding_value")
    private String bindingValue;

    /**
     * 优先级（数字越大优先级越高）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 工厂 ID
     */
    @TableField("factory_id")
    private Long factoryId;
}

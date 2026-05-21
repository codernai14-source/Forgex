package com.forgex.basic.label.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签字段实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_label_field")
public class LabelField extends BaseEntity {

    @TableField("field_code")
    private String fieldCode;

    @TableField("field_name")
    private String fieldName;

    @TableField("field_type")
    private String fieldType;

    @TableField("module_id")
    private Long moduleId;

    @TableField("is_enabled")
    private Boolean isEnabled;
}

package com.forgex.basic.label.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签类型实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_label_type")
public class LabelType extends BaseEntity {

    @TableField("type_code")
    private String typeCode;

    @TableField("type_name")
    private String typeName;

    @TableField("is_enabled")
    private Boolean isEnabled;
}

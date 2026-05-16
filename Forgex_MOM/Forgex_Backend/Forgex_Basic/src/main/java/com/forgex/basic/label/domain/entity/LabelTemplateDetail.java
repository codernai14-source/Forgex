package com.forgex.basic.label.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签模板组件详情实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_label_template_detail")
public class LabelTemplateDetail extends BaseEntity {

    @TableField("template_id")
    private Long templateId;

    @TableField("component_type")
    private String componentType;

    @TableField("position_x")
    private Integer positionX;

    @TableField("position_y")
    private Integer positionY;

    @TableField("component_width")
    private Integer componentWidth;

    @TableField("component_height")
    private Integer componentHeight;

    @TableField("component_content")
    private String componentContent;

    @TableField("data_source")
    private String dataSource;

    @TableField("field_code")
    private String fieldCode;

    @TableField("style_json")
    private String styleJson;

    @TableField("sort_no")
    private Integer sortNo;
}

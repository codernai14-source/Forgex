package com.forgex.basic.label.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标签渲染组件。
 */
@Data
@Schema(description = "标签渲染组件")
public class LabelRenderComponentVO {

    @Schema(description = "组件类型")
    private String componentType;

    @Schema(description = "X坐标")
    private Integer positionX;

    @Schema(description = "Y坐标")
    private Integer positionY;

    @Schema(description = "宽度")
    private Integer componentWidth;

    @Schema(description = "高度")
    private Integer componentHeight;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "字段编码")
    private String fieldCode;

    @Schema(description = "数据来源")
    private String dataSource;

    @Schema(description = "样式JSON")
    private String styleJson;

    @Schema(description = "排序")
    private Integer sortNo;
}

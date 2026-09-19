package com.forgex.basic.label.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 标签渲染结果。
 */
@Data
@Schema(description = "标签渲染结果")
public class LabelRenderVO {

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "纸张宽度")
    private Integer paperWidth;

    @Schema(description = "纸张高度")
    private Integer paperHeight;

    @Schema(description = "纸张规格")
    private String paperSize;

    @Schema(description = "打印张数")
    private Integer printCount;

    @Schema(description = "组件列表")
    private List<LabelRenderComponentVO> components;
}

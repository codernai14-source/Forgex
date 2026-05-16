package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签模板保存参数。
 */
@Data
@Schema(description = "标签模板保存参数")
public class LabelTemplateSaveParam {

    @NotBlank(message = "模板编码不能为空")
    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateCode;

    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateName;

    @Schema(description = "模板类型")
    private String templateType;

    @Schema(description = "标签类型ID")
    private Long typeId;

    @Schema(description = "纸张宽度")
    private Integer paperWidth;

    @Schema(description = "纸张高度")
    private Integer paperHeight;

    @Schema(description = "纸张规格")
    private String paperSize;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    @Schema(description = "旧模板内容")
    private String templateContent;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态")
    private Integer status;
}

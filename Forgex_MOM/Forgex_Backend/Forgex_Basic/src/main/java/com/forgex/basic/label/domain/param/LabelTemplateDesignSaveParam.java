package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 标签模板设计保存参数。
 */
@Data
@Schema(description = "标签模板设计保存参数")
public class LabelTemplateDesignSaveParam {

    @NotNull(message = "模板ID不能为空")
    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long templateId;

    @NotBlank(message = "模板编码不能为空")
    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateCode;

    @Valid
    @Schema(description = "模板组件列表")
    private List<LabelTemplateDetailParam> details;
}

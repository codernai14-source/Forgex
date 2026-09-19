package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 标签字段保存参数。
 */
@Data
@Schema(description = "标签字段保存参数")
public class LabelFieldSaveParam {

    @NotBlank(message = "字段编码不能为空")
    @Schema(description = "字段编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldCode;

    @NotBlank(message = "字段名称不能为空")
    @Schema(description = "字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldName;

    @NotBlank(message = "字段类型不能为空")
    @Schema(description = "字段类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldType;

    @NotNull(message = "模块ID不能为空")
    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long moduleId;

    @Schema(description = "是否启用")
    private Boolean isEnabled;
}

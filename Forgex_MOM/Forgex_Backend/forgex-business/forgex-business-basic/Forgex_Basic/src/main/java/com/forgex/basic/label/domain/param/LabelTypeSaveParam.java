package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签类型保存参数。
 */
@Data
@Schema(description = "标签类型保存参数")
public class LabelTypeSaveParam {

    @NotBlank(message = "类型编码不能为空")
    @Schema(description = "类型编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String typeCode;

    @NotBlank(message = "类型名称不能为空")
    @Schema(description = "类型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String typeName;

    @Schema(description = "是否启用")
    private Boolean isEnabled;
}

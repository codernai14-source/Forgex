package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 标签字段更新参数。
 */
@Data
@Schema(description = "标签字段更新参数")
public class LabelFieldUpdateParam {

    @NotNull(message = "ID不能为空")
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "字段编码")
    private String fieldCode;

    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "模块ID")
    private Long moduleId;

    @Schema(description = "是否启用")
    private Boolean isEnabled;
}

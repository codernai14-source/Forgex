package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 标签类型更新参数。
 */
@Data
@Schema(description = "标签类型更新参数")
public class LabelTypeUpdateParam {

    @NotNull(message = "ID不能为空")
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "类型编码")
    private String typeCode;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "是否启用")
    private Boolean isEnabled;
}

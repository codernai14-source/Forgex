package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 标签渲染参数。
 */
@Data
@Schema(description = "标签渲染参数")
public class LabelPrintRenderParam {

    @NotBlank(message = "模板编码不能为空")
    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateCode;

    @NotNull(message = "打印数据不能为空")
    @Schema(description = "打印数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private Object data;

    @NotNull(message = "打印张数不能为空")
    @Min(value = 1, message = "打印张数必须大于0")
    @Schema(description = "打印张数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer printCount;
}

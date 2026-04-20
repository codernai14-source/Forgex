// 2. JSON 校验结果 VO
package com.forgex.basic.label.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "模板 JSON 校验结果 VO")
public class TemplateValidateResultVO {

    @Schema(description = "是否通过校验")
    private Boolean valid;

    @Schema(description = "错误列表")
    private List<String> errors;

    @Schema(description = "警告列表")
    private List<String> warnings;

    @Schema(description = "提取的占位符列表")
    private List<String> placeholders;
}

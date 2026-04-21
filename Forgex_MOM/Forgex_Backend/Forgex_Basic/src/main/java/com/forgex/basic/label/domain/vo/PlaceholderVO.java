// 1. 占位符 VO
package com.forgex.basic.label.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "标准占位符 VO")
public class PlaceholderVO {

    @Schema(description = "字段名")
    private String fieldName;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "字段描述")
    private String description;

    @Schema(description = "是否必填")
    private Boolean required;

    @Schema(description = "示例值")
    private String exampleValue;

    @Schema(description = "分组（物料/供应商/客户/系统）")
    private String group;
}

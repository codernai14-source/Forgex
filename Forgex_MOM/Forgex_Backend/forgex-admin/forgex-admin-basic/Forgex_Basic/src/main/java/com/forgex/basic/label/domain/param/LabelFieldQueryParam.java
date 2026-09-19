package com.forgex.basic.label.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签字段查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标签字段查询参数")
public class LabelFieldQueryParam extends BaseGetParam {

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

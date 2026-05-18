package com.forgex.basic.label.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签类型查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标签类型查询参数")
public class LabelTypeQueryParam extends BaseGetParam {

    @Schema(description = "类型编码")
    private String typeCode;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "是否启用")
    private Boolean isEnabled;
}

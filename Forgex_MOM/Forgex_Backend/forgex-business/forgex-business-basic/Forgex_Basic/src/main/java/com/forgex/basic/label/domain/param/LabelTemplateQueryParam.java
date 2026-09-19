package com.forgex.basic.label.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签模板查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标签模板查询参数")
public class LabelTemplateQueryParam extends BaseGetParam {

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板类型")
    private String templateType;

    @Schema(description = "标签类型ID")
    private Long typeId;

    @Schema(description = "是否默认")
    private Boolean isDefault;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    @Schema(description = "状态")
    private Integer status;
}

package com.forgex.basic.label.domain.vo;

import com.forgex.common.api.annotation.AutoFillUsername;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签字段视图对象。
 */
@Data
@Schema(description = "标签字段VO")
public class LabelFieldVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "字段编码")
    private String fieldCode;

    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "模块ID")
    private Long moduleId;

    @Schema(description = "模块名称")
    private String moduleName;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "创建人")
    private String createBy;

    @AutoFillUsername(userIdField = "createBy")
    private String createByName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}

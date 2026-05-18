package com.forgex.basic.label.domain.vo;

import com.forgex.common.api.annotation.AutoFillUsername;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签类型视图对象。
 */
@Data
@Schema(description = "标签类型VO")
public class LabelTypeVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "类型编码")
    private String typeCode;

    @Schema(description = "类型名称")
    private String typeName;

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

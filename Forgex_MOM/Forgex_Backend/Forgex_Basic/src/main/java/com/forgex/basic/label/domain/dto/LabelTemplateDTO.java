package com.forgex.basic.label.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签模板数据传输对象。
 */
@Data
@Schema(description = "标签模板DTO")
public class LabelTemplateDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板类型")
    private String templateType;

    @Schema(description = "标签类型ID")
    private Long typeId;

    @Schema(description = "纸张宽度")
    private Integer paperWidth;

    @Schema(description = "纸张高度")
    private Integer paperHeight;

    @Schema(description = "纸张规格")
    private String paperSize;

    @Schema(description = "模板版本")
    private Integer templateVersion;

    @Schema(description = "是否默认")
    private Boolean isDefault;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    @Schema(description = "模板内容")
    private String templateContent;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "工厂ID")
    private Long factoryId;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}

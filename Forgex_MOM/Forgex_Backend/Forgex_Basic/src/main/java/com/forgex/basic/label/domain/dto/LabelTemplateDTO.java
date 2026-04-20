package com.forgex.basic.label.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签模板数据传输对象
 * <p>
 * 用于标签模板的数据传输和展示
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "标签模板 DTO")
public class LabelTemplateDTO {

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;

    /**
     * 模板编码
     */
    @Schema(description = "模板编码")
    private String templateCode;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 模板类型
     */
    @Schema(description = "模板类型")
    private String templateType;

    /**
     * 模板版本号
     */
    @Schema(description = "模板版本号")
    private Integer templateVersion;

    /**
     * 是否默认模板：0-否，1-是
     */
    @Schema(description = "是否默认模板")
    private Boolean isDefault;

    /**
     * 模板内容 JSON
     */
    @Schema(description = "模板内容 JSON")
    private String templateContent;

    /**
     * 模板描述
     */
    @Schema(description = "模板描述")
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 工厂 ID
     */
    @Schema(description = "工厂 ID")
    private Long factoryId;

    /**
     * 租户 ID
     */
    @Schema(description = "租户 ID")
    private Long tenantId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 标签模板保存参数
 * <p>
 * 用于新增标签模板的请求参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "标签模板保存参数")
public class LabelTemplateSaveParam {

    /**
     * 模板编码
     */
    @NotBlank(message = "模板编码不能为空")
    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateCode;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateName;

    /**
     * 模板类型
     */
    @NotBlank(message = "模板类型不能为空")
    @Schema(description = "模板类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateType;

    /**
     * 模板内容 JSON
     */
    @NotBlank(message = "模板内容不能为空")
    @Schema(description = "模板内容 JSON", requiredMode = Schema.RequiredMode.REQUIRED)
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
     * 工厂 ID（NULL 表示全局模板）
     */
    @Schema(description = "工厂 ID")
    private Long factoryId;
}

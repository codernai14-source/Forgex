package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 标签模板更新参数
 * <p>
 * 用于更新标签模板的请求参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "标签模板更新参数")
public class LabelTemplateUpdateParam {

    /**
     * 模板 ID
     */
    @NotNull(message = "模板 ID 不能为空")
    @Schema(description = "模板 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    private String templateName;

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
}


package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 标签绑定保存参数
 * <p>
 * 用于创建或更新标签模板绑定关系的请求参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "标签绑定保存参数")
public class LabelBindingSaveParam {

    /**
     * 绑定 ID（更新时必填）
     */
    @Schema(description = "绑定 ID")
    private Long id;

    /**
     * 模板 ID
     */
    @NotNull(message = "模板 ID 不能为空")
    @Schema(description = "模板 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long templateId;

    /**
     * 绑定类型：MATERIAL=按物料，SUPPLIER=按供应商，CUSTOMER=按客户
     */
    @NotBlank(message = "绑定类型不能为空")
    @Schema(description = "绑定类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bindingType;

    /**
     * 绑定值（物料编码/供应商编码/客户编码）
     */
    @NotBlank(message = "绑定值不能为空")
    @Schema(description = "绑定值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bindingValue;

    /**
     * 优先级（数字越大优先级越高）
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 工厂 ID
     */
    @Schema(description = "工厂 ID")
    private Long factoryId;
}

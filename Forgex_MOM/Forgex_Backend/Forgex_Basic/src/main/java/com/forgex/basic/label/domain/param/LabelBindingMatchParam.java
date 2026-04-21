package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 标签模板匹配参数
 * <p>
 * 用于根据业务条件匹配最合适模板的请求参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "标签模板匹配参数")
public class LabelBindingMatchParam {

    /**
     * 工厂 ID
     */
    @NotNull(message = "工厂 ID 不能为空")
    @Schema(description = "工厂 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long factoryId;

    /**
     * 模板类型
     */
    @NotBlank(message = "模板类型不能为空")
    @Schema(description = "模板类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateType;

    /**
     * 物料 ID
     */
    @Schema(description = "物料 ID")
    private Long materialId;

    /**
     * 供应商 ID
     */
    @Schema(description = "供应商 ID")
    private Long supplierId;

    /**
     * 客户 ID
     */
    @Schema(description = "客户 ID")
    private Long customerId;
}

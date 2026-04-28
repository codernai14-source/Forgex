package com.forgex.basic.supplier.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供应商审查工作流回调参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
@Data
@Schema(description = "供应商审查工作流回调参数")
public class SupplierWorkflowCallbackParam {

    /**
     * 审批执行 ID。
     */
    @Schema(description = "审批执行 ID")
    private Long executionId;

    /**
     * 审批任务编码。
     */
    @Schema(description = "审批任务编码")
    private String taskCode;

    /**
     * 审批任务名称。
     */
    @Schema(description = "审批任务名称")
    private String taskName;

    /**
     * 审批状态。
     */
    @Schema(description = "审批状态")
    private Integer status;

    /**
     * 表单内容 JSON。
     */
    @Schema(description = "表单内容 JSON")
    private String formContent;
}

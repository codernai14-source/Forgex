package com.forgex.workflow.domain.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批人撤回参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-07-07
 */
@Data
public class WfExecutionRecallParam {

    /**
     * 审批执行 ID。
     */
    @NotNull(message = "执行ID不能为空")
    private Long executionId;

    /**
     * 已处理的审批实例 ID。
     */
    @NotNull(message = "审批实例ID不能为空")
    private Long approvalInstanceId;

    /**
     * 撤回说明。
     */
    private String comment;
}

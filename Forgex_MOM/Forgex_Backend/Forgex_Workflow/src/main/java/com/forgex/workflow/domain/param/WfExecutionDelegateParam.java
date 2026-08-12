package com.forgex.workflow.domain.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 单条审批待办委托参数。
 */
@Data
public class WfExecutionDelegateParam {

    /**
     * 审批执行 ID。
     */
    @NotNull(message = "执行ID不能为空")
    private Long executionId;

    /**
     * 当前审批实例 ID。
     */
    @NotNull(message = "审批实例ID不能为空")
    private Long approvalInstanceId;

    /**
     * 受托审批人 ID。
     */
    @NotNull(message = "受托人不能为空")
    private Long targetApproverId;

    /**
     * 委托说明。
     */
    private String comment;
}

package com.forgex.workflow.domain.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WfExecutionTransferParam {

    @NotNull(message = "执行ID不能为空")
    private Long executionId;

    @NotNull(message = "审批实例ID不能为空")
    private Long approvalInstanceId;

    @NotNull(message = "目标审批人不能为空")
    private Long targetApproverId;

    private String comment;
}

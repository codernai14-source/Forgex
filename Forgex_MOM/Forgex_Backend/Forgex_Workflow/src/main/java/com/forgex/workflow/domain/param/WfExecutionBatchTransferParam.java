package com.forgex.workflow.domain.param;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WfExecutionBatchTransferParam {

    @NotEmpty(message = "执行单不能为空")
    private List<Long> executionIds = new ArrayList<>();

    @NotNull(message = "目标审批人不能为空")
    private Long targetApproverId;

    private String comment;
}

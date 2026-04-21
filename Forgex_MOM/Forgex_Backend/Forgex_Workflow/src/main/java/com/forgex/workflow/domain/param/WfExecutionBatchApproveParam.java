package com.forgex.workflow.domain.param;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WfExecutionBatchApproveParam {

    @NotEmpty(message = "执行单不能为空")
    private List<Long> executionIds = new ArrayList<>();

    @NotNull(message = "审批状态不能为空")
    private Integer approveStatus;

    private String comment;
}

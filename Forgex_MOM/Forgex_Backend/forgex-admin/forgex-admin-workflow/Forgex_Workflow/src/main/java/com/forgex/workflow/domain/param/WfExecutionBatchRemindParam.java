package com.forgex.workflow.domain.param;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WfExecutionBatchRemindParam {

    @NotEmpty(message = "执行单不能为空")
    private List<Long> executionIds = new ArrayList<>();

    private String comment;
}

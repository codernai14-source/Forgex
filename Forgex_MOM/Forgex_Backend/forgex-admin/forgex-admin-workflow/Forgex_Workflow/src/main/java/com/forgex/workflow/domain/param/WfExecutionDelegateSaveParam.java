package com.forgex.workflow.domain.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WfExecutionDelegateSaveParam {

    @NotNull(message = "委托人不能为空")
    private Long delegatorUserId;

    @NotNull(message = "受托人不能为空")
    private Long delegateUserId;

    private String comment;
}

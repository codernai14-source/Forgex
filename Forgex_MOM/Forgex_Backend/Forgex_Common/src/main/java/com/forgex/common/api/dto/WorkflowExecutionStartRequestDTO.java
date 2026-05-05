package com.forgex.common.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 工作流发起请求 DTO
 * <p>
 * 供业务模块通过公共 Feign 契约发起审批流程。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@Data
public class WorkflowExecutionStartRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务编码
     */
    @NotBlank(message = "任务编码不能为空")
    private String taskCode;

    /**
     * 表单内容 JSON
     */
    @NotNull(message = "表单内容不能为空")
    private String formContent;

    /**
     * 发起人选择的审批人列表。
     */
    private List<Long> selectedApprovers = new ArrayList<>();
}

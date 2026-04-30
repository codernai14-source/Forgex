package com.forgex.common.api.feign;

import com.forgex.common.api.dto.WorkflowExecutionStartRequestDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 工作流执行内部 Feign 客户端
 * <p>
 * 供业务模块以统一契约发起审批流程。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@FeignClient(name = "forgex-workflow", contextId = "workflowExecutionFeignClient", path = "/wf/execution/internal")
public interface WorkflowExecutionFeignClient {

    /**
     * 内部发起审批流程
     *
     * @param param 发起参数
     * @return executionId
     */
    @PostMapping("/start")
    R<Long> startExecution(@RequestBody WorkflowExecutionStartRequestDTO param);
}

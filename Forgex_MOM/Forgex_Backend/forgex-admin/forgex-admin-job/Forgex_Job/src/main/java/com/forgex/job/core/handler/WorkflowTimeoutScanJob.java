package com.forgex.job.core.handler;

import com.forgex.common.api.dto.WorkflowTimeoutScanRequestDTO;
import com.forgex.common.api.feign.WorkflowExecutionFeignClient;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.forgex.job.annotation.FxJobHandler;
import com.forgex.job.core.executor.JobExecutionContext;
import com.forgex.job.core.executor.JobResult;
import lombok.extern.slf4j.Slf4j;

/**
 * 工作流超时补偿定时任务处理器。
 * <p>
 * 周期性扫描当前租户下已超时且仍处于待办的审批实例，通过 Feign 触发工作流模块按节点规则配置的
 * 超时动作（提醒 / 自动通过 / 自动转交）分派处理。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@FxJobHandler("workflowTimeoutScanJob")
public class WorkflowTimeoutScanJob {

    private final WorkflowExecutionFeignClient workflowExecutionFeignClient;

    public WorkflowTimeoutScanJob(WorkflowExecutionFeignClient workflowExecutionFeignClient) {
        this.workflowExecutionFeignClient = workflowExecutionFeignClient;
    }

    /**
     * 执行工作流超时扫描任务。
     *
     * @param context Job 执行上下文
     * @return 执行结果
     */
    public JobResult execute(JobExecutionContext context) {
        Long tenantId = context.getTenantId();
        if (tenantId == null) {
            return JobResult.failure("工作流超时扫描缺少租户 ID，请在任务定义中指定租户");
        }

        WorkflowTimeoutScanRequestDTO request = new WorkflowTimeoutScanRequestDTO();
        request.setTenantId(tenantId);

        R<Boolean> response = workflowExecutionFeignClient.scanTimeoutInstances(request);
        if (response == null || response.getCode() == null || response.getCode() != StatusCode.SUCCESS) {
            String reason = response == null ? "no response" : response.getMessage();
            return JobResult.failure("工作流超时扫描失败, tenantId=" + tenantId + ", reason=" + reason);
        }
        return JobResult.success("工作流超时扫描完成, tenantId=" + tenantId + ", handled=" + response.getData());
    }
}

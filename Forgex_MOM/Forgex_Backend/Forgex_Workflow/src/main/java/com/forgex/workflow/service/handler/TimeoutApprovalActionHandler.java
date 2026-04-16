package com.forgex.workflow.service.handler;

import com.forgex.workflow.domain.param.WfExecutionCompensateParam;
import com.forgex.workflow.service.impl.WfExecutionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 超时任务动作处理器。
 * <p>
 * 负责接收补偿中心或定时治理入口触发的超时重试请求，
 * 并委派给执行服务中的超时处理主逻辑完成状态修正。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 */
@Component
@RequiredArgsConstructor
public class TimeoutApprovalActionHandler implements ApprovalActionHandler<WfExecutionCompensateParam> {

    /**
     * 延迟注入执行服务，避免超时处理器参与执行服务初始化闭环。
     */
    @Lazy
    private final WfExecutionServiceImpl executionService;

    /**
     * 获取当前处理器支持的动作类型。
     *
     * @return 超时动作类型
     */
    @Override
    public ApprovalActionHandlerType getType() {
        return ApprovalActionHandlerType.TIMEOUT;
    }

    /**
     * 执行超时任务重试。
     *
     * @param param 补偿与重试参数
     * @return 处理结果
     */
    @Override
    public Boolean handle(WfExecutionCompensateParam param) {
        return executionService.handleRetryTimeoutJobsAction(param);
    }
}

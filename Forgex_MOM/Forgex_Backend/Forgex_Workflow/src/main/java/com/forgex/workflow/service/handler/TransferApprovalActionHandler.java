package com.forgex.workflow.service.handler;

import com.forgex.workflow.domain.param.WfExecutionTransferParam;
import com.forgex.workflow.service.impl.WfExecutionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 转交动作处理器。
 * <p>
 * 负责接收转交审批请求，并调用执行服务中的转交主流程完成实例关闭、目标实例创建和日志记录。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 */
@Component
@RequiredArgsConstructor
public class TransferApprovalActionHandler implements ApprovalActionHandler<WfExecutionTransferParam> {

    /**
     * 延迟注入执行服务，避免处理器工厂装配阶段形成循环依赖。
     */
    @Lazy
    private final WfExecutionServiceImpl executionService;

    /**
     * 获取当前处理器支持的动作类型。
     *
     * @return 转交动作类型
     */
    @Override
    public ApprovalActionHandlerType getType() {
        return ApprovalActionHandlerType.TRANSFER;
    }

    /**
     * 执行转交动作。
     *
     * @param param 转交参数
     * @return 处理结果
     */
    @Override
    public Boolean handle(WfExecutionTransferParam param) {
        return executionService.handleTransferAction(param);
    }
}

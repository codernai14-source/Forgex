package com.forgex.workflow.service.handler;

import com.forgex.workflow.domain.param.WfExecutionDelegateSaveParam;
import com.forgex.workflow.service.impl.WfExecutionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 委托动作处理器。
 * <p>
 * 负责接收委托设置请求，并调用执行服务中的委托主流程完成实例改派、待办重建和动作日志记录。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 */
@Component
@RequiredArgsConstructor
public class DelegateApprovalActionHandler implements ApprovalActionHandler<WfExecutionDelegateSaveParam> {

    /**
     * 延迟注入执行服务，避免委托处理器在 Bean 创建期反向拉起执行服务。
     */
    @Lazy
    private final WfExecutionServiceImpl executionService;

    /**
     * 获取当前处理器支持的动作类型。
     *
     * @return 委托动作类型
     */
    @Override
    public ApprovalActionHandlerType getType() {
        return ApprovalActionHandlerType.DELEGATE;
    }

    /**
     * 执行委托动作。
     *
     * @param param 委托参数
     * @return 处理结果
     */
    @Override
    public Boolean handle(WfExecutionDelegateSaveParam param) {
        return executionService.handleSaveDelegateAction(param);
    }
}

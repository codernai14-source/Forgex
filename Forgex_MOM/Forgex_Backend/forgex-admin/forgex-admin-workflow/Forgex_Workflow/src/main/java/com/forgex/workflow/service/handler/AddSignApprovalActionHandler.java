package com.forgex.workflow.service.handler;

import com.forgex.workflow.domain.param.WfExecutionAddSignParam;
import com.forgex.workflow.service.impl.WfExecutionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 加签动作处理器。
 * <p>
 * 负责接收加签审批请求，并调用执行服务中的加签主流程完成新实例创建与动作日志记录。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 */
@Component
@RequiredArgsConstructor
public class AddSignApprovalActionHandler implements ApprovalActionHandler<WfExecutionAddSignParam> {

    /**
     * 延迟注入执行服务，避免 “执行服务 -> 处理器工厂 -> 处理器 -> 执行服务” 循环依赖。
     */
    @Lazy
    private final WfExecutionServiceImpl executionService;

    /**
     * 获取当前处理器支持的动作类型。
     *
     * @return 加签动作类型
     */
    @Override
    public ApprovalActionHandlerType getType() {
        return ApprovalActionHandlerType.ADD_SIGN;
    }

    /**
     * 执行加签动作。
     *
     * @param param 加签参数
     * @return 处理结果
     */
    @Override
    public Boolean handle(WfExecutionAddSignParam param) {
        return executionService.handleAddSignAction(param);
    }
}

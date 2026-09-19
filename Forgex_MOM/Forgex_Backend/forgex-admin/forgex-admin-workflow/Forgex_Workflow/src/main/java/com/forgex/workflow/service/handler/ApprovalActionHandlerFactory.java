package com.forgex.workflow.service.handler;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 审批动作处理器工厂。
 * <p>
 * 在 Spring 启动时收集全部处理器，并建立动作类型到处理器实例的映射，
 * 供执行服务在事务边界内按类型调用对应处理器。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 */
@Component
public class ApprovalActionHandlerFactory {

    private final Map<ApprovalActionHandlerType, ApprovalActionHandler<?>> handlerMap;

    /**
     * 构造处理器工厂。
     *
     * @param handlers Spring 注入的全部动作处理器
     */
    public ApprovalActionHandlerFactory(List<ApprovalActionHandler<?>> handlers) {
        this.handlerMap = new EnumMap<>(ApprovalActionHandlerType.class);
        for (ApprovalActionHandler<?> handler : handlers) {
            this.handlerMap.put(handler.getType(), handler);
        }
    }

    /**
     * 根据动作类型获取处理器。
     *
     * @param type 动作类型
     * @param <T> 处理器入参类型
     * @return 对应动作处理器
     */
    @SuppressWarnings("unchecked")
    public <T> ApprovalActionHandler<T> getHandler(ApprovalActionHandlerType type) {
        ApprovalActionHandler<?> handler = handlerMap.get(type);
        if (handler == null) {
            throw new IllegalStateException("No approval action handler for type: " + type);
        }
        return (ApprovalActionHandler<T>) handler;
    }
}

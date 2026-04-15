package com.forgex.workflow.service.handler;

/**
 * 审批动作处理器接口。
 * <p>
 * 用于将转交、加签、委托、超时重试等运营动作从执行服务中拆出，
 * 由工厂统一按动作类型分发到具体处理器执行。
 * </p>
 *
 * @param <T> 处理器入参类型
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 */
public interface ApprovalActionHandler<T> {

    /**
     * 获取处理器支持的动作类型。
     *
     * @return 动作类型枚举
     */
    ApprovalActionHandlerType getType();

    /**
     * 执行动作处理。
     *
     * @param param 处理参数
     * @return 处理结果，成功返回 true
     */
    Boolean handle(T param);
}

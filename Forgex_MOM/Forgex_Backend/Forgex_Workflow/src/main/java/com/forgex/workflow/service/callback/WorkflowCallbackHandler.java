package com.forgex.workflow.service.callback;

import java.util.Map;

/**
 * 工作流本地回调处理器。
 * <p>
 * 当审批任务配置了 {@code callbackBean} 且未配置 {@code callbackUrl} 时，
 * 工作流结束回调会从当前 Spring 容器按 Bean 名称查找该接口实现并传入回调载荷。
 * 若同时配置 URL 与 Bean，仅走 HTTP 通道。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface WorkflowCallbackHandler {

    /**
     * 处理工作流回调。
     *
     * @param payload 回调载荷，包含 executionId、taskCode、taskName、status、formContent
     */
    void handle(Map<String, Object> payload);
}

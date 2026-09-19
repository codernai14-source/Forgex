package com.forgex.integration.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 集成接口执行结果。
 * <p>
 * 同时承载同步调用结果、异步任务提交结果以及多目标出站调用结果。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
@Builder
public class IntegrationExecuteResult {

    /**
     * 请求是否被平台受理。
     */
    private boolean accepted;

    /**
     * 是否执行成功。
     */
    private boolean success;

    /**
     * 异步任务 ID。
     */
    private String taskId;

    /**
     * 调用链路 ID。
     */
    private String traceId;

    /**
     * 执行状态。
     */
    private String status;

    /**
     * 结果类型。
     */
    private String resultType;

    /**
     * 返回数据。
     */
    private Object data;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 同步目标执行结果。
     */
    @Builder.Default
    private List<IntegrationTargetResult> syncResults = new ArrayList<>();

    /**
     * 异步目标提交结果。
     */
    @Builder.Default
    private List<IntegrationTargetResult> asyncResults = new ArrayList<>();
}

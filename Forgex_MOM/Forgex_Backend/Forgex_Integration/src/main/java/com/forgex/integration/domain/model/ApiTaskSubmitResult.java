package com.forgex.integration.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * 异步任务提交结果
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@Builder
public class ApiTaskSubmitResult {

    private String taskId;

    private String traceId;

    private String status;

    private Long outboundTargetId;

    private String targetSystemCode;

    private String targetSystemName;
}

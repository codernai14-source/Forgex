package com.forgex.integration.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API 执行上下文。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@Builder
public class ApiExecutionContext {

    private Long tenantId;

    private Long apiConfigId;

    private Long outboundTargetId;

    private String apiCode;

    private String targetSystemCode;

    private String targetSystemName;

    private String direction;

    private String traceId;

    private String taskId;

    private String callerIp;

    private String invokeMode;

    private LocalDateTime startTime;
}

package com.forgex.integration.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 鎺ュ彛鎵ц涓婁笅鏂?
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

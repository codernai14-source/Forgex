package com.forgex.integration.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * 寮傛浠诲姟鎻愪氦缁撴灉
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

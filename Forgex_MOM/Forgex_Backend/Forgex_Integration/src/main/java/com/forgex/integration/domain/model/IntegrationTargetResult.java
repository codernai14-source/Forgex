package com.forgex.integration.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IntegrationTargetResult {

    private boolean accepted;

    private boolean success;

    private Long outboundTargetId;

    private String targetSystemCode;

    private String targetSystemName;

    private String taskId;

    private String traceId;

    private String status;

    private String resultType;

    private Object data;

    private String errorMessage;
}

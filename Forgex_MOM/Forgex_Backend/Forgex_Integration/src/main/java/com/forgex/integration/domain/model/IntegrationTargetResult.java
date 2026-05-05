package com.forgex.integration.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * integration目标结果实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
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

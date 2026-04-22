package com.forgex.integration.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * 缁熶竴鍏叡鎵ц缁撴灉
 */
@Data
@Builder
public class IntegrationExecuteResult {

    private boolean accepted;

    private boolean success;

    private String taskId;

    private String traceId;

    private String status;

    private String resultType;

    private Object data;

    private String errorMessage;
}

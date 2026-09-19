package com.forgex.job.core.executor;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Job 执行上下文。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@Builder
public class JobExecutionContext {
    private Long tenantId;
    private Long jobId;
    private String jobCode;
    private Long logId;
    private Integer triggerType;
    private String params;
    private LocalDateTime fireTime;
    private Integer shardIndex;
    private Integer shardTotal;
}

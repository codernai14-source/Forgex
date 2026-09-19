package com.forgex.job.domain.vo;

import lombok.Data;

/**
 * Job 大盘摘要。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
public class JobDashboardSummaryVO {
    private Long totalTasks;
    private Long enabledTasks;
    private Long todayExecutions;
    private Long successExecutions;
    private Long failedExecutions;
    private Long timeoutExecutions;
    private Long onlineInstances;
    private Double successRate;
}

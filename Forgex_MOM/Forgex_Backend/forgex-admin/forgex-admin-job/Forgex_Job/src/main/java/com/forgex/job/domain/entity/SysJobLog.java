package com.forgex.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务执行日志实体。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job_log")
public class SysJobLog extends BaseEntity {
    private Long jobId;
    private String jobCode;
    private String jobName;
    private Integer triggerType;
    private LocalDateTime fireTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private Integer status;
    private String instanceId;
    private String requestParams;
    private String resultMessage;
    private String errorStack;
    private Integer retryCount;
    private Long retryOfLogId;
    private Integer shardIndex;
    private Integer shardTotal;
    private String requestId;
}

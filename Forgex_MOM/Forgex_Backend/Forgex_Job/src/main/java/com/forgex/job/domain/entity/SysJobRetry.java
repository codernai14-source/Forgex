package com.forgex.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 重试与死信实体。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job_retry")
public class SysJobRetry extends BaseEntity {
    private Long jobId;
    private Long logId;
    private String jobCode;
    private String bizType;
    private String bizId;
    private Integer retryCount;
    private Integer maxRetryCount;
    private LocalDateTime nextRetryTime;
    private Integer status;
    private String lastError;
    private String handleRemark;
}

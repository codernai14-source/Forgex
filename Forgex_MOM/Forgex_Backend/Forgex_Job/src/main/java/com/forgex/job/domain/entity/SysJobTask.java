package com.forgex.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务定义实体。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job_task")
public class SysJobTask extends BaseEntity {
    private String jobCode;
    private String jobName;
    private String jobGroup;
    private Integer jobType;
    private Integer scheduleType;
    private String cronExpression;
    private Integer intervalSeconds;
    private String beanName;
    private String methodName;
    private String httpUrl;
    private String httpMethod;
    private String httpHeaders;
    private String scriptType;
    private String scriptPath;
    private String scriptArgs;
    private String mqTopic;
    private String mqTags;
    private Long workflowId;
    private String jobParams;
    private Integer status;
    private Integer blockStrategy;
    private Integer timeoutSeconds;
    private Integer maxRetryCount;
    private Integer retryIntervalSeconds;
    private Integer shardTotal;
    private Integer broadcastEnabled;
    private LocalDateTime nextTriggerTime;
    private LocalDateTime lastTriggerTime;
    private Integer lastStatus;
    private Long triggerCount;
    private String remark;
}

package com.forgex.job.domain.param;

import lombok.Data;

/**
 * 任务保存参数。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
public class JobTaskSaveParam {
    private Long id;
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
    private String remark;
}

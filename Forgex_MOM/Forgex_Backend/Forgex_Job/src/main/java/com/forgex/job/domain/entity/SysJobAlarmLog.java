package com.forgex.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 告警日志实体。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job_alarm_log")
public class SysJobAlarmLog extends BaseEntity {
    private Long ruleId;
    private Long jobId;
    private String jobCode;
    private Long logId;
    private Integer alarmType;
    private Integer sendStatus;
    private String notifyType;
    private String notifyTarget;
    private String content;
    private String errorMessage;
}

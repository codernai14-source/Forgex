package com.forgex.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 告警规则实体。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job_alarm_rule")
public class SysJobAlarmRule extends BaseEntity {
    private String ruleName;
    private Long jobId;
    private String jobCode;
    private Integer alarmType;
    private Integer thresholdCount;
    private Integer windowMinutes;
    private String notifyType;
    private String notifyTarget;
    private Integer status;
    private String remark;
}

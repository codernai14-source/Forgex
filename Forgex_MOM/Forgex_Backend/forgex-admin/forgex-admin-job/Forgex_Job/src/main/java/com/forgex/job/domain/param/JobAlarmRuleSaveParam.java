package com.forgex.job.domain.param;

import lombok.Data;

/**
 * 告警规则保存参数。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
public class JobAlarmRuleSaveParam {
    private Long id;
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

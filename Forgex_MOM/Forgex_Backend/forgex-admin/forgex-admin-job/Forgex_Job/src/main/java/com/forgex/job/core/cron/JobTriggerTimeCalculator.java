package com.forgex.job.core.cron;

import com.forgex.job.domain.entity.SysJobTask;
import com.forgex.job.enums.JobConstants;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 任务下次触发时间计算器。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Component
public class JobTriggerTimeCalculator {

    public LocalDateTime next(SysJobTask task, LocalDateTime baseTime) {
        if (task == null || task.getScheduleType() == null) {
            return null;
        }
        LocalDateTime base = baseTime == null ? LocalDateTime.now() : baseTime;
        if (JobConstants.SCHEDULE_CRON == task.getScheduleType()) {
            if (!StringUtils.hasText(task.getCronExpression())) {
                return null;
            }
            return CronExpression.parse(task.getCronExpression()).next(base);
        }
        if (JobConstants.SCHEDULE_INTERVAL == task.getScheduleType()) {
            Integer seconds = task.getIntervalSeconds();
            if (seconds == null || seconds <= 0) {
                return null;
            }
            LocalDateTime last = task.getLastTriggerTime() == null ? base : task.getLastTriggerTime();
            return (last.isAfter(base) ? last : base).plusSeconds(seconds);
        }
        return null;
    }

    public void validateCron(String expression) {
        if (StringUtils.hasText(expression)) {
            CronExpression.parse(expression);
        }
    }
}

package com.forgex.job.core.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.job.config.JobProperties;
import com.forgex.job.core.cron.JobTriggerTimeCalculator;
import com.forgex.job.core.executor.JobExecutor;
import com.forgex.job.core.lock.JobLockService;
import com.forgex.job.domain.entity.SysJobLog;
import com.forgex.job.domain.entity.SysJobTask;
import com.forgex.job.enums.JobConstants;
import com.forgex.job.service.IJobLogService;
import com.forgex.job.service.IJobTaskService;
import org.redisson.api.RLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 到期任务扫描器。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Component
public class JobScheduleScanner {

    private final JobProperties properties;
    private final IJobTaskService jobTaskService;
    private final IJobLogService jobLogService;
    private final JobExecutor jobExecutor;
    private final JobTriggerTimeCalculator calculator;
    private final JobLockService lockService;

    public JobScheduleScanner(JobProperties properties,
                              IJobTaskService jobTaskService,
                              IJobLogService jobLogService,
                              JobExecutor jobExecutor,
                              JobTriggerTimeCalculator calculator,
                              JobLockService lockService) {
        this.properties = properties;
        this.jobTaskService = jobTaskService;
        this.jobLogService = jobLogService;
        this.jobExecutor = jobExecutor;
        this.calculator = calculator;
        this.lockService = lockService;
    }

    @Scheduled(fixedDelayString = "${forgex.job.scheduler.scan-interval-ms:1000}")
    public void scan() {
        if (!properties.isEnabled()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<SysJobTask> tasks = jobTaskService.list(new LambdaQueryWrapper<SysJobTask>()
            .eq(SysJobTask::getStatus, JobConstants.STATUS_ENABLED)
            .ne(SysJobTask::getScheduleType, JobConstants.SCHEDULE_MANUAL)
            .le(SysJobTask::getNextTriggerTime, now.plusSeconds(properties.getScheduler().getLookAheadSeconds()))
            .orderByAsc(SysJobTask::getNextTriggerTime)
            .last("limit 100"));
        for (SysJobTask task : tasks) {
            handle(task);
        }
    }

    private void handle(SysJobTask task) {
        LocalDateTime fireTime = task.getNextTriggerTime() == null ? LocalDateTime.now() : task.getNextTriggerTime();
        String key = "forgex:job:lock:" + task.getTenantId() + ":" + task.getId() + ":" + fireTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        RLock lock = lockService.getLock(key);
        try {
            if (!lockService.tryLock(lock, properties.getScheduler().getLockWaitMs(), properties.getScheduler().getLockLeaseSeconds())) {
                return;
            }
            if (isBlocked(task)) {
                return;
            }
            SysJobTask update = new SysJobTask();
            update.setId(task.getId());
            update.setNextTriggerTime(calculator.next(task, LocalDateTime.now()));
            jobTaskService.updateById(update);
            jobExecutor.submit(task, JobConstants.TRIGGER_SCHEDULE, task.getJobParams(), null);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            lockService.unlockQuietly(lock);
        }
    }

    private boolean isBlocked(SysJobTask task) {
        if (task.getBlockStrategy() == null || JobConstants.BLOCK_PARALLEL == task.getBlockStrategy()) {
            return false;
        }
        Long running = jobLogService.count(new LambdaQueryWrapper<SysJobLog>()
            .eq(SysJobLog::getJobId, task.getId())
            .eq(SysJobLog::getStatus, JobConstants.LOG_RUNNING));
        return running != null && running > 0;
    }
}

package com.forgex.job.core.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.job.config.JobProperties;
import com.forgex.job.domain.entity.SysJobLog;
import com.forgex.job.domain.entity.SysJobTask;
import com.forgex.job.enums.JobConstants;
import com.forgex.job.mapper.SysJobLogMapper;
import com.forgex.job.mapper.SysJobTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 僵尸 RUNNING 日志回收扫描器。
 * <p>
 * 当执行节点在任务运行期间宕机、进程被强杀或线程被意外中断时，对应执行日志可能永久停留在
 * RUNNING 状态。本扫描器周期性回收超过任务超时时长和误触发阈值的 RUNNING 日志。
 * </p>
 *
 * @author Forgex
 * @version 1.0.0
 */
@Slf4j
@Component
public class JobZombieReclaimScanner {

    private static final long DEFAULT_TIMEOUT_SECONDS = 60L;

    private final JobProperties properties;
    private final SysJobLogMapper jobLogMapper;
    private final SysJobTaskMapper jobTaskMapper;

    public JobZombieReclaimScanner(JobProperties properties,
                                   SysJobLogMapper jobLogMapper,
                                   SysJobTaskMapper jobTaskMapper) {
        this.properties = properties;
        this.jobLogMapper = jobLogMapper;
        this.jobTaskMapper = jobTaskMapper;
    }

    /**
     * 扫描并回收僵尸 RUNNING 日志。
     */
    @Scheduled(fixedDelayString = "${forgex.job.zombie-reclaim.scan-interval-ms:30000}")
    public void scan() {
        if (!properties.isEnabled()) {
            return;
        }
        long misfireSeconds = Math.max(0, properties.getScheduler().getMisfireThresholdSeconds());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime candidateCutoff = now.minusSeconds(DEFAULT_TIMEOUT_SECONDS + misfireSeconds);
        List<SysJobLog> runningLogs = jobLogMapper.selectList(new LambdaQueryWrapper<SysJobLog>()
                .eq(SysJobLog::getStatus, JobConstants.LOG_RUNNING)
                .isNotNull(SysJobLog::getStartTime)
                .lt(SysJobLog::getStartTime, candidateCutoff)
                .last("limit 200"));
        if (runningLogs.isEmpty()) {
            return;
        }
        int reclaimed = 0;
        for (SysJobLog runningLog : runningLogs) {
            long timeoutSeconds = resolveTaskTimeoutSeconds(runningLog.getJobId());
            LocalDateTime deadline = runningLog.getStartTime().plusSeconds(timeoutSeconds + misfireSeconds);
            if (deadline.isAfter(now)) {
                continue;
            }
            if (markZombieTimeout(runningLog, now, timeoutSeconds, misfireSeconds)) {
                reclaimed++;
            }
        }
        if (reclaimed > 0) {
            log.warn("回收僵尸 RUNNING 任务日志 {} 条，misfireThresholdSeconds={}", reclaimed, misfireSeconds);
        }
    }

    private long resolveTaskTimeoutSeconds(Long jobId) {
        if (jobId == null) {
            return DEFAULT_TIMEOUT_SECONDS;
        }
        SysJobTask task = jobTaskMapper.selectById(jobId);
        if (task == null || task.getTimeoutSeconds() == null || task.getTimeoutSeconds() <= 0) {
            return DEFAULT_TIMEOUT_SECONDS;
        }
        return task.getTimeoutSeconds();
    }

    private boolean markZombieTimeout(SysJobLog runningLog,
                                      LocalDateTime now,
                                      long timeoutSeconds,
                                      long misfireSeconds) {
        SysJobLog update = new SysJobLog();
        update.setStatus(JobConstants.LOG_TIMEOUT);
        update.setEndTime(now);
        if (runningLog.getStartTime() != null) {
            update.setDurationMs(Duration.between(runningLog.getStartTime(), now).toMillis());
        }
        update.setResultMessage("僵尸任务回收：RUNNING 超过 " + (timeoutSeconds + misfireSeconds)
                + "s 未结束，判定为超时");
        int rows = jobLogMapper.update(update, new LambdaQueryWrapper<SysJobLog>()
                .eq(SysJobLog::getId, runningLog.getId())
                .eq(SysJobLog::getStatus, JobConstants.LOG_RUNNING));
        return rows == 1;
    }
}

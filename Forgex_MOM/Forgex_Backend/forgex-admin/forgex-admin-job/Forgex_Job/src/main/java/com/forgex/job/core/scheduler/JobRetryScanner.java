package com.forgex.job.core.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.job.core.executor.JobExecutor;
import com.forgex.job.domain.entity.SysJobRetry;
import com.forgex.job.domain.entity.SysJobTask;
import com.forgex.job.enums.JobConstants;
import com.forgex.job.mapper.SysJobRetryMapper;
import com.forgex.job.service.IJobTaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 失败重试扫描器。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Component
public class JobRetryScanner {

    private final SysJobRetryMapper retryMapper;
    private final IJobTaskService jobTaskService;
    private final JobExecutor jobExecutor;

    public JobRetryScanner(SysJobRetryMapper retryMapper, IJobTaskService jobTaskService, JobExecutor jobExecutor) {
        this.retryMapper = retryMapper;
        this.jobTaskService = jobTaskService;
        this.jobExecutor = jobExecutor;
    }

    @Scheduled(fixedDelayString = "${forgex.job.retry.scan-interval-ms:5000}")
    public void scan() {
        List<SysJobRetry> retries = retryMapper.selectList(new LambdaQueryWrapper<SysJobRetry>()
            .eq(SysJobRetry::getStatus, JobConstants.RETRY_WAITING)
            .le(SysJobRetry::getNextRetryTime, LocalDateTime.now())
            .last("limit 50"));
        for (SysJobRetry retry : retries) {
            SysJobTask task = jobTaskService.getById(retry.getJobId());
            if (task == null) {
                retry.setStatus(JobConstants.RETRY_DEAD);
                retryMapper.updateById(retry);
                continue;
            }
            if (retry.getRetryCount() != null && retry.getMaxRetryCount() != null
                && retry.getRetryCount() >= retry.getMaxRetryCount()) {
                retry.setStatus(JobConstants.RETRY_DEAD);
                retryMapper.updateById(retry);
                continue;
            }
            retry.setRetryCount((retry.getRetryCount() == null ? 0 : retry.getRetryCount()) + 1);
            retry.setNextRetryTime(LocalDateTime.now().plusSeconds(task.getRetryIntervalSeconds() == null ? 60 : task.getRetryIntervalSeconds()));
            retryMapper.updateById(retry);
            jobExecutor.submit(task, JobConstants.TRIGGER_RETRY, task.getJobParams(), "retry-" + retry.getId() + "-" + retry.getRetryCount());
        }
    }
}

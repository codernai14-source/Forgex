package com.forgex.job.core.executor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.job.config.JobProperties;
import com.forgex.job.domain.entity.SysJobLog;
import com.forgex.job.domain.entity.SysJobRetry;
import com.forgex.job.domain.entity.SysJobTask;
import com.forgex.job.enums.JobConstants;
import com.forgex.job.mapper.SysJobRetryMapper;
import com.forgex.job.mapper.SysJobTaskMapper;
import com.forgex.job.service.IJobInstanceService;
import com.forgex.job.service.IJobLogService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Job 执行器。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Component
public class JobExecutor {

    private final JavaBeanJobInvoker javaBeanJobInvoker;
    private final HttpJobInvoker httpJobInvoker;
    private final ScriptJobInvoker scriptJobInvoker;
    private final IJobLogService jobLogService;
    private final IJobInstanceService jobInstanceService;
    private final SysJobTaskMapper jobTaskMapper;
    private final SysJobRetryMapper retryMapper;
    private final JobProperties properties;
    private final Executor executor;

    public JobExecutor(JavaBeanJobInvoker javaBeanJobInvoker,
                       HttpJobInvoker httpJobInvoker,
                       ScriptJobInvoker scriptJobInvoker,
                       IJobLogService jobLogService,
                       IJobInstanceService jobInstanceService,
                       SysJobTaskMapper jobTaskMapper,
                       SysJobRetryMapper retryMapper,
                       JobProperties properties,
                       @Qualifier("jobTaskExecutor") Executor executor) {
        this.javaBeanJobInvoker = javaBeanJobInvoker;
        this.httpJobInvoker = httpJobInvoker;
        this.scriptJobInvoker = scriptJobInvoker;
        this.jobLogService = jobLogService;
        this.jobInstanceService = jobInstanceService;
        this.jobTaskMapper = jobTaskMapper;
        this.retryMapper = retryMapper;
        this.properties = properties;
        this.executor = executor;
    }

    public Long submit(SysJobTask task, Integer triggerType, String params, String requestId) {
        SysJobLog log = jobLogService.createRunningLog(task, triggerType, params, requestId);
        CompletableFuture.runAsync(() -> execute(task, log, triggerType, params), executor);
        return log.getId();
    }

    private void execute(SysJobTask task, SysJobLog log, Integer triggerType, String params) {
        jobInstanceService.increaseRunning();
        Long previousTenant = TenantContext.get();
        try {
            TenantContext.set(task.getTenantId());
            JobExecutionContext context = JobExecutionContext.builder()
                .tenantId(task.getTenantId())
                .jobId(task.getId())
                .jobCode(task.getJobCode())
                .logId(log.getId())
                .triggerType(triggerType)
                .params(params)
                .fireTime(log.getFireTime())
                .shardIndex(log.getShardIndex())
                .shardTotal(log.getShardTotal())
                .build();
            CompletableFuture<JobResult> future = CompletableFuture.supplyAsync(() -> invoke(task, context), executor);
            JobResult result = future.get(resolveTimeout(task), TimeUnit.SECONDS);
            if (result.isSuccess()) {
                jobLogService.markSuccess(log.getId(), result.getMessage());
                updateTaskLast(task, JobConstants.LOG_SUCCESS);
            } else {
                jobLogService.markFailed(log.getId(), new IllegalStateException(result.getMessage()), properties.getLog().getStackMaxLength());
                updateTaskLast(task, JobConstants.LOG_FAILED);
                createRetryIfNeeded(task, log, result.getMessage());
            }
        } catch (java.util.concurrent.TimeoutException ex) {
            jobLogService.markTimeout(log.getId(), "timeout");
            updateTaskLast(task, JobConstants.LOG_TIMEOUT);
            createRetryIfNeeded(task, log, "timeout");
        } catch (Exception ex) {
            jobLogService.markFailed(log.getId(), ex, properties.getLog().getStackMaxLength());
            updateTaskLast(task, JobConstants.LOG_FAILED);
            createRetryIfNeeded(task, log, ex.getMessage());
        } finally {
            if (previousTenant == null) {
                TenantContext.clear();
            } else {
                TenantContext.set(previousTenant);
            }
            jobInstanceService.decreaseRunning();
        }
    }

    private JobResult invoke(SysJobTask task, JobExecutionContext context) {
        try {
            return switch (task.getJobType() == null ? JobConstants.JOB_TYPE_JAVA_BEAN : task.getJobType()) {
                case JobConstants.JOB_TYPE_HTTP -> httpJobInvoker.invoke(task, context);
                case JobConstants.JOB_TYPE_SCRIPT -> scriptJobInvoker.invoke(task);
                case JobConstants.JOB_TYPE_ROCKETMQ -> JobResult.success("RocketMQ event accepted: " + task.getMqTopic());
                case JobConstants.JOB_TYPE_WORKFLOW -> JobResult.success("Workflow task accepted: " + task.getWorkflowId());
                default -> javaBeanJobInvoker.invoke(task, context);
            };
        } catch (Exception ex) {
            return JobResult.failure(ex.getMessage());
        }
    }

    private long resolveTimeout(SysJobTask task) {
        return task.getTimeoutSeconds() == null || task.getTimeoutSeconds() <= 0 ? 60 : task.getTimeoutSeconds();
    }

    private void updateTaskLast(SysJobTask task, Integer status) {
        SysJobTask update = new SysJobTask();
        update.setId(task.getId());
        update.setLastStatus(status);
        update.setLastTriggerTime(LocalDateTime.now());
        update.setTriggerCount((task.getTriggerCount() == null ? 0L : task.getTriggerCount()) + 1);
        jobTaskMapper.updateById(update);
    }

    private void createRetryIfNeeded(SysJobTask task, SysJobLog log, String error) {
        int maxRetry = task.getMaxRetryCount() == null ? 0 : task.getMaxRetryCount();
        if (maxRetry <= 0) {
            return;
        }
        Long exists = retryMapper.selectCount(new LambdaQueryWrapper<SysJobRetry>()
            .eq(SysJobRetry::getLogId, log.getId()));
        if (exists != null && exists > 0) {
            return;
        }
        SysJobRetry retry = new SysJobRetry();
        retry.setTenantId(task.getTenantId());
        retry.setJobId(task.getId());
        retry.setLogId(log.getId());
        retry.setJobCode(task.getJobCode());
        retry.setRetryCount(0);
        retry.setMaxRetryCount(maxRetry);
        retry.setNextRetryTime(LocalDateTime.now().plusSeconds(task.getRetryIntervalSeconds() == null ? 60 : task.getRetryIntervalSeconds()));
        retry.setStatus(JobConstants.RETRY_WAITING);
        retry.setLastError(error);
        retryMapper.insert(retry);
    }
}

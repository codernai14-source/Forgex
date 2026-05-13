package com.forgex.job.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.job.domain.entity.SysJobLog;
import com.forgex.job.domain.entity.SysJobTask;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.enums.JobConstants;
import com.forgex.job.mapper.SysJobLogMapper;
import com.forgex.job.service.IJobLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Job 日志服务实现。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Service
@DS("job")
public class JobLogServiceImpl extends ServiceImpl<SysJobLogMapper, SysJobLog> implements IJobLogService {

    @Override
    public IPage<SysJobLog> pageLogs(JobPageParam param) {
        JobPageParam query = param == null ? new JobPageParam() : param;
        LambdaQueryWrapper<SysJobLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getJobId() != null, SysJobLog::getJobId, query.getJobId());
        wrapper.like(StringUtils.hasText(query.getJobCode()), SysJobLog::getJobCode, query.getJobCode());
        wrapper.like(StringUtils.hasText(query.getJobName()), SysJobLog::getJobName, query.getJobName());
        wrapper.eq(query.getTriggerType() != null, SysJobLog::getTriggerType, query.getTriggerType());
        wrapper.eq(query.getStatus() != null, SysJobLog::getStatus, query.getStatus());
        wrapper.like(StringUtils.hasText(query.getInstanceId()), SysJobLog::getInstanceId, query.getInstanceId());
        wrapper.orderByDesc(SysJobLog::getStartTime);
        return page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public SysJobLog createRunningLog(SysJobTask task, Integer triggerType, String params, String requestId) {
        SysJobLog log = new SysJobLog();
        log.setTenantId(task.getTenantId());
        log.setJobId(task.getId());
        log.setJobCode(task.getJobCode());
        log.setJobName(task.getJobName());
        log.setTriggerType(triggerType);
        log.setFireTime(LocalDateTime.now());
        log.setStartTime(LocalDateTime.now());
        log.setStatus(JobConstants.LOG_RUNNING);
        log.setRequestParams(params);
        log.setRetryCount(0);
        log.setRequestId(requestId);
        save(log);
        return log;
    }

    @Override
    public void markSuccess(Long logId, String message) {
        SysJobLog log = getById(logId);
        if (log == null) {
            return;
        }
        LocalDateTime end = LocalDateTime.now();
        log.setEndTime(end);
        log.setDurationMs(duration(log.getStartTime(), end));
        log.setStatus(JobConstants.LOG_SUCCESS);
        log.setResultMessage(truncate(message, 1000));
        updateById(log);
    }

    @Override
    public void markFailed(Long logId, Throwable throwable, int maxStackLength) {
        SysJobLog log = getById(logId);
        if (log == null) {
            return;
        }
        LocalDateTime end = LocalDateTime.now();
        log.setEndTime(end);
        log.setDurationMs(duration(log.getStartTime(), end));
        log.setStatus(JobConstants.LOG_FAILED);
        log.setResultMessage(truncate(throwable == null ? "failed" : throwable.getMessage(), 1000));
        log.setErrorStack(truncate(stack(throwable), maxStackLength));
        updateById(log);
    }

    @Override
    public void markTimeout(Long logId, String message) {
        SysJobLog log = getById(logId);
        if (log == null) {
            return;
        }
        LocalDateTime end = LocalDateTime.now();
        log.setEndTime(end);
        log.setDurationMs(duration(log.getStartTime(), end));
        log.setStatus(JobConstants.LOG_TIMEOUT);
        log.setResultMessage(truncate(message, 1000));
        updateById(log);
    }

    private Long duration(LocalDateTime start, LocalDateTime end) {
        return start == null || end == null ? 0L : Duration.between(start, end).toMillis();
    }

    private String stack(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || maxLength <= 0 || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}

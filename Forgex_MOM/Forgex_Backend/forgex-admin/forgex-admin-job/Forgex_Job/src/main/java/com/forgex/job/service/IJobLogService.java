package com.forgex.job.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.job.domain.entity.SysJobLog;
import com.forgex.job.domain.entity.SysJobTask;
import com.forgex.job.domain.param.JobPageParam;

/**
 * Job 日志服务。
 *
 * @author Forgex
 * @version 1.0.0
 */
public interface IJobLogService extends IService<SysJobLog> {
    IPage<SysJobLog> pageLogs(JobPageParam param);
    SysJobLog createRunningLog(SysJobTask task, Integer triggerType, String params, String requestId);
    void markSuccess(Long logId, String message);
    void markFailed(Long logId, Throwable throwable, int maxStackLength);
    void markTimeout(Long logId, String message);
}

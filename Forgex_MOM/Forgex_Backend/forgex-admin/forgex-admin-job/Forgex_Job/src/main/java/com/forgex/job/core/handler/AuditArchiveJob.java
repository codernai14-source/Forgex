package com.forgex.job.core.handler;

import com.forgex.job.annotation.FxJobHandler;
import com.forgex.job.core.executor.JobExecutionContext;
import com.forgex.job.core.executor.JobResult;
import com.forgex.sys.service.IDataBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 审计日志冷归档任务。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@FxJobHandler("auditArchiveJob")
@RequiredArgsConstructor
public class AuditArchiveJob {

    private final IDataBackupService dataBackupService;

    /**
     * 归档超过 180 天的操作日志。
     *
     * @param context 任务上下文
     * @return 执行结果
     */
    public JobResult execute(JobExecutionContext context) {
        int archived = dataBackupService.archiveAuditLogs(180);
        return JobResult.success("archived=" + archived);
    }
}

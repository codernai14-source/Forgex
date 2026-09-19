package com.forgex.job.core.handler;

import com.forgex.job.annotation.FxJobHandler;
import com.forgex.job.core.executor.JobExecutionContext;
import com.forgex.job.core.executor.JobResult;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.forgex.job.service.PlatformBackupClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据库全量备份任务。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@FxJobHandler("databaseBackupJob")
@RequiredArgsConstructor
public class DatabaseBackupJob {

    private final PlatformBackupClient backupClient;

    /**
     * 触发一次全量备份记录。
     *
     * @param context 任务上下文
     * @return 执行结果
     */
    public JobResult execute(JobExecutionContext context) {
        R<Long> response = backupClient.triggerFullBackup(context.getTenantId());
        if (response == null || !Integer.valueOf(StatusCode.SUCCESS).equals(response.getCode())
                || response.getData() == null) {
            return JobResult.failure("backup failed, code=" + (response == null ? "no response" : response.getCode()));
        }
        return JobResult.success("backupId=" + response.getData());
    }
}

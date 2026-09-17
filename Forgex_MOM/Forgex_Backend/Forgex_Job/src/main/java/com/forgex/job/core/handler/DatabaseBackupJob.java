package com.forgex.job.core.handler;

import com.forgex.job.annotation.FxJobHandler;
import com.forgex.job.core.executor.JobExecutionContext;
import com.forgex.job.core.executor.JobResult;
import com.forgex.sys.domain.entity.SysDataBackupRecord;
import com.forgex.sys.service.IDataBackupService;
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

    private final IDataBackupService dataBackupService;

    /**
     * 触发一次全量备份记录。
     *
     * @param context 任务上下文
     * @return 执行结果
     */
    public JobResult execute(JobExecutionContext context) {
        SysDataBackupRecord record = dataBackupService.triggerFullBackup("job");
        return JobResult.success("backupId=" + record.getId());
    }
}

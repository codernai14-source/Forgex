package com.forgex.job.core.handler;

import com.forgex.common.web.R;
import com.forgex.job.core.executor.JobExecutionContext;
import com.forgex.job.core.executor.JobResult;
import com.forgex.job.service.PlatformBackupClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 验证调度结果保留远端业务失败并携带任务租户。 */
class PlatformBackupJobsTest {

    private final PlatformBackupClient client = mock(PlatformBackupClient.class);
    private final JobExecutionContext context = JobExecutionContext.builder().tenantId(123L).build();

    @Test
    void backupReturnsRemoteRecordId() {
        when(client.triggerFullBackup(123L)).thenReturn(R.ok(456L));
        JobResult result = new DatabaseBackupJob(client).execute(context);
        assertTrue(result.isSuccess());
        assertEquals("backupId=456", result.getMessage());
        verify(client).triggerFullBackup(123L);
    }

    @Test
    void backupDoesNotTreatFailureOrEmptyResponseAsSuccess() {
        when(client.triggerFullBackup(123L)).thenReturn(R.fail(), R.ok(), null);
        DatabaseBackupJob job = new DatabaseBackupJob(client);
        assertFalse(job.execute(context).isSuccess());
        assertFalse(job.execute(context).isSuccess());
        assertFalse(job.execute(context).isSuccess());
    }

    @Test
    void archiveAcceptsZeroAsSuccessfulCount() {
        when(client.archiveAuditLogs(123L)).thenReturn(R.ok(0));
        JobResult result = new AuditArchiveJob(client).execute(context);
        assertTrue(result.isSuccess());
        assertEquals("archived=0", result.getMessage());
        verify(client).archiveAuditLogs(123L);
    }

    @Test
    void archiveDoesNotTreatFailureOrEmptyResponseAsSuccess() {
        when(client.archiveAuditLogs(123L)).thenReturn(R.fail(), R.ok(), null);
        AuditArchiveJob job = new AuditArchiveJob(client);
        assertFalse(job.execute(context).isSuccess());
        assertFalse(job.execute(context).isSuccess());
        assertFalse(job.execute(context).isSuccess());
    }
}

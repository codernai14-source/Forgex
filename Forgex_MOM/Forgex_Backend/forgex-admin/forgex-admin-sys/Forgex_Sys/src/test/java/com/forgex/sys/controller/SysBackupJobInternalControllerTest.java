package com.forgex.sys.controller;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.sys.api.dto.SysBackupJobRequestDTO;
import com.forgex.sys.api.feign.SysBackupJobFeignClient;
import com.forgex.sys.domain.entity.SysDataBackupRecord;
import com.forgex.sys.service.IDataBackupService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 验证新 HTTP 边界拒绝伪造、重放及跨租户调用，并恢复执行线程上下文。 */
class SysBackupJobInternalControllerTest {

    private static final String TICKET = "a".repeat(64);
    private static final String KEY = SysBackupJobFeignClient.TICKET_PREFIX + TICKET;
    private final IDataBackupService backupService = mock(IDataBackupService.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final SysBackupJobInternalController controller = new SysBackupJobInternalController(backupService, redis);
    private final SysBackupJobRequestDTO request = new SysBackupJobRequestDTO(10L);

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(values);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void missingMalformedAndUnissuedTicketsCannotExecuteBackup() {
        assertThrows(I18nBusinessException.class, () -> controller.triggerFullBackup(null, request));
        assertThrows(I18nBusinessException.class, () -> controller.triggerFullBackup("user-token", request));
        assertThrows(I18nBusinessException.class, () -> controller.triggerFullBackup(TICKET, request));
        verifyNoInteractions(backupService);
    }

    @Test
    void ticketCannotBeReusedForAnotherTenantOrOperation() {
        when(values.getAndDelete(KEY)).thenReturn("backup:11", "archive:10");
        assertThrows(I18nBusinessException.class, () -> controller.triggerFullBackup(TICKET, request));
        assertThrows(I18nBusinessException.class, () -> controller.triggerFullBackup(TICKET, request));
        verifyNoInteractions(backupService);
    }

    @Test
    void acceptedTicketRunsAsItsTenantAndCannotBeReplayed() {
        TenantContext.set(999L);
        when(values.getAndDelete(KEY)).thenReturn("backup:10", null);
        when(backupService.triggerFullBackup("job")).thenAnswer(invocation -> {
            assertEquals(10L, TenantContext.get());
            SysDataBackupRecord record = new SysDataBackupRecord();
            record.setId(12L);
            return record;
        });
        assertEquals(12L, controller.triggerFullBackup(TICKET, request).getData());
        assertEquals(999L, TenantContext.get());
        assertThrows(I18nBusinessException.class, () -> controller.triggerFullBackup(TICKET, request));
        verify(backupService, times(1)).triggerFullBackup("job");
    }

    @Test
    void archiveUsesOriginalRetentionAndRestoresTenantOnFailure() {
        when(values.getAndDelete(KEY)).thenReturn("archive:10");
        when(backupService.archiveAuditLogs(180)).thenAnswer(invocation -> {
            assertEquals(10L, TenantContext.get());
            throw new IllegalStateException("storage failed");
        });
        assertThrows(IllegalStateException.class, () -> controller.archiveAuditLogs(TICKET, request));
        assertNull(TenantContext.get());
    }

    @Test
    void archiveReturnsOriginalCount() {
        when(values.getAndDelete(KEY)).thenReturn("archive:10");
        when(backupService.archiveAuditLogs(180)).thenReturn(17);
        assertEquals(17, controller.archiveAuditLogs(TICKET, request).getData());
        verify(backupService).archiveAuditLogs(180);
    }
}

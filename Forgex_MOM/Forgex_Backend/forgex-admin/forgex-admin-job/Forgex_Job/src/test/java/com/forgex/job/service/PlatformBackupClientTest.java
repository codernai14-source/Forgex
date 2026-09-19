package com.forgex.job.service;

import com.forgex.common.web.R;
import com.forgex.sys.api.dto.SysBackupJobRequestDTO;
import com.forgex.sys.api.feign.SysBackupJobFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 验证调用凭证的租户及操作绑定、生命周期和失败清理。 */
class PlatformBackupClientTest {

    private final SysBackupJobFeignClient feign = mock(SysBackupJobFeignClient.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final PlatformBackupClient client = new PlatformBackupClient(feign, redis);

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(values);
    }

    @Test
    void backupIssuesScopedShortLivedTicketAndCleansItAfterCall() {
        when(feign.triggerFullBackup(anyString(), any())).thenReturn(R.ok(5L));
        assertEquals(5L, client.triggerFullBackup(10L).getData());
        ArgumentCaptor<String> ticket = ArgumentCaptor.forClass(String.class);
        verify(feign).triggerFullBackup(ticket.capture(), eq(new SysBackupJobRequestDTO(10L)));
        assertTrue(ticket.getValue().matches("[0-9a-f]{64}"));
        String key = SysBackupJobFeignClient.TICKET_PREFIX + ticket.getValue();
        verify(values).set(key, "backup:10", Duration.ofSeconds(60));
        verify(redis).delete(key);
    }

    @Test
    void archiveUsesDistinctScopeAndCleansTicketOnNetworkFailure() {
        when(feign.archiveAuditLogs(anyString(), any())).thenThrow(new IllegalStateException("offline"));
        assertThrows(IllegalStateException.class, () -> client.archiveAuditLogs(10L));
        ArgumentCaptor<String> ticket = ArgumentCaptor.forClass(String.class);
        verify(feign).archiveAuditLogs(ticket.capture(), eq(new SysBackupJobRequestDTO(10L)));
        String key = SysBackupJobFeignClient.TICKET_PREFIX + ticket.getValue();
        verify(values).set(key, "archive:10", Duration.ofSeconds(60));
        verify(redis).delete(key);
    }

    @Test
    void redisFailurePreventsRemoteExecution() {
        doThrow(new IllegalStateException("offline")).when(values).set(anyString(), anyString(), any(Duration.class));
        assertThrows(IllegalStateException.class, () -> client.triggerFullBackup(10L));
        verifyNoInteractions(feign);
    }

    @Test
    void cleanupFailureDoesNotOverwriteSuccessfulBackup() {
        when(feign.triggerFullBackup(anyString(), any())).thenReturn(R.ok(5L));
        when(redis.delete(anyString())).thenThrow(new IllegalStateException("offline"));
        assertEquals(5L, client.triggerFullBackup(10L).getData());
    }
}

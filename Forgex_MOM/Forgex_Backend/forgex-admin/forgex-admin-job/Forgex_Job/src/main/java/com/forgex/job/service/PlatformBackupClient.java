package com.forgex.job.service;

import com.forgex.common.web.R;
import com.forgex.sys.api.dto.SysBackupJobRequestDTO;
import com.forgex.sys.api.feign.SysBackupJobFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.function.Function;

/**
 * 调度器调用平台备份接口，签发短期凭证以替代原进程内的可信调用。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PlatformBackupClient {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final SysBackupJobFeignClient backupFeignClient;
    private final StringRedisTemplate redisTemplate;

    /**
     * 触发全量备份。
     *
     * @param tenantId 任务租户
     * @return 备份记录 ID
     */
    public R<Long> triggerFullBackup(Long tenantId) {
        return invoke(SysBackupJobFeignClient.BACKUP_ACTION, tenantId,
                ticket -> backupFeignClient.triggerFullBackup(ticket, new SysBackupJobRequestDTO(tenantId)));
    }

    /**
     * 归档超过 180 天的日志。
     *
     * @param tenantId 任务租户
     * @return 归档条数
     */
    public R<Integer> archiveAuditLogs(Long tenantId) {
        return invoke(SysBackupJobFeignClient.ARCHIVE_ACTION, tenantId,
                ticket -> backupFeignClient.archiveAuditLogs(ticket, new SysBackupJobRequestDTO(tenantId)));
    }

    private <T> R<T> invoke(String action, Long tenantId, Function<String, R<T>> call) {
        // 每次尝试独立签发，凭证不进入日志、配置或用户会话。
        byte[] entropy = new byte[32];
        RANDOM.nextBytes(entropy);
        String ticket = HexFormat.of().formatHex(entropy);
        String key = SysBackupJobFeignClient.TICKET_PREFIX + ticket;
        redisTemplate.opsForValue().set(key,
                SysBackupJobFeignClient.ticketScope(action, tenantId), Duration.ofSeconds(60));
        try {
            return call.apply(ticket);
        } finally {
            // 接收端已消费时删除为空；网络失败时仍及时回收未消费凭证。
            try {
                redisTemplate.delete(key);
            } catch (RuntimeException cleanupFailure) {
                // 凭证仍受 TTL 限制，清理故障不能覆盖远端实际执行结果而造成重复调度。
                log.warn("Backup job ticket cleanup failed; ticket will expire automatically ({})",
                        cleanupFailure.getClass().getSimpleName());
            }
        }
    }
}

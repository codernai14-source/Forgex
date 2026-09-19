package com.forgex.sys.api.feign;

import com.forgex.common.web.R;
import com.forgex.sys.api.dto.SysBackupJobRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 定时任务调用系统备份能力的显式契约。
 * 调用凭证由受信任平台进程写入共享 Redis，接收端消费一次后立即失效。
 */
@FeignClient(name = "forgex-sys", contextId = "sysBackupJobFeignClient", path = "/sys/backup/internal")
public interface SysBackupJobFeignClient {

    /** 内部任务凭证请求头。 */
    String TICKET_HEADER = "X-Forgex-Job-Ticket";
    /** 凭证存储前缀，凭证有效期为 60 秒。 */
    String TICKET_PREFIX = "fx:job:backup:ticket:";
    /** 全量备份操作标识。 */
    String BACKUP_ACTION = "backup";
    /** 审计归档操作标识。 */
    String ARCHIVE_ACTION = "archive";

    /**
     * 由平台任务触发一次全量备份。
     *
     * @param ticket 一次性调用凭证
     * @param request 任务租户
     * @return 备份记录 ID
     */
    @PostMapping("/run")
    R<Long> triggerFullBackup(@RequestHeader(TICKET_HEADER) String ticket,
                             @RequestBody SysBackupJobRequestDTO request);

    /**
     * 归档任务租户下超过 180 天的审计日志。
     *
     * @param ticket 一次性调用凭证
     * @param request 任务租户
     * @return 归档记录数
     */
    @PostMapping("/archive-audit")
    R<Integer> archiveAuditLogs(@RequestHeader(TICKET_HEADER) String ticket,
                               @RequestBody SysBackupJobRequestDTO request);

    /**
     * 构造调用凭证绑定内容，防止凭证被用于其他操作或租户。
     *
     * @param action 操作标识
     * @param tenantId 任务租户
     * @return 凭证绑定内容
     */
    static String ticketScope(String action, Long tenantId) {
        return action + ":" + tenantId;
    }
}

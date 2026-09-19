package com.forgex.sys.controller;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.api.dto.SysBackupJobRequestDTO;
import com.forgex.sys.api.feign.SysBackupJobFeignClient;
import com.forgex.sys.service.IDataBackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

/**
 * 定时任务专用备份接口；必须先消费平台 Redis 中的操作及租户绑定凭证。
 * 用户的登录令牌或可伪造的用户请求头均不能替代此凭证。
 */
@RestController
@RequestMapping("/backup/internal")
@RequiredArgsConstructor
public class SysBackupJobInternalController {

    private final IDataBackupService dataBackupService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 触发任务全量备份，操作人保持为 job。
     *
     * @param ticket 一次性任务调用凭证
     * @param request 任务租户
     * @return 备份记录 ID
     */
    @PostMapping("/run")
    public R<Long> triggerFullBackup(
            @RequestHeader(value = SysBackupJobFeignClient.TICKET_HEADER, required = false) String ticket,
            @RequestBody SysBackupJobRequestDTO request) {
        authorize(ticket, request, SysBackupJobFeignClient.BACKUP_ACTION);
        return withTenant(request.tenantId(), () -> R.ok(dataBackupService.triggerFullBackup("job").getId()));
    }

    /**
     * 归档任务租户下超过 180 天的审计日志。
     *
     * @param ticket 一次性任务调用凭证
     * @param request 任务租户
     * @return 归档条数
     */
    @PostMapping("/archive-audit")
    public R<Integer> archiveAuditLogs(
            @RequestHeader(value = SysBackupJobFeignClient.TICKET_HEADER, required = false) String ticket,
            @RequestBody SysBackupJobRequestDTO request) {
        authorize(ticket, request, SysBackupJobFeignClient.ARCHIVE_ACTION);
        return withTenant(request.tenantId(), () -> R.ok(dataBackupService.archiveAuditLogs(180)));
    }

    private void authorize(String ticket, SysBackupJobRequestDTO request, String action) {
        if (request == null || ticket == null || !ticket.matches("[0-9a-f]{64}")) {
            throw new I18nBusinessException(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION);
        }
        // GETDEL 保证并发重放最多一次通过，操作或租户不匹配也立即作废。
        String scope = redisTemplate.opsForValue().getAndDelete(SysBackupJobFeignClient.TICKET_PREFIX + ticket);
        if (!SysBackupJobFeignClient.ticketScope(action, request.tenantId()).equals(scope)) {
            throw new I18nBusinessException(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION);
        }
    }

    private <T> T withTenant(Long tenantId, Supplier<T> action) {
        Long previous = TenantContext.get();
        try {
            if (tenantId == null) {
                TenantContext.clear();
            } else {
                TenantContext.set(tenantId);
            }
            return action.get();
        } finally {
            if (previous == null) {
                TenantContext.clear();
            } else {
                TenantContext.set(previous);
            }
        }
    }
}

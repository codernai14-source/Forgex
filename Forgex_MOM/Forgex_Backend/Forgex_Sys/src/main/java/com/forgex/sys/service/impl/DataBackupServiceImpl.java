package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.crypto.SM3Utils;
import com.forgex.common.crypto.SM4FileEncryptor;
import com.forgex.common.tenant.TenantContext;
import com.forgex.sys.domain.entity.SysDataBackupRecord;
import com.forgex.sys.domain.entity.SysOperationLog;
import com.forgex.sys.domain.entity.SysUserPasswordHistory;
import com.forgex.sys.mapper.SysDataBackupRecordMapper;
import com.forgex.sys.mapper.SysOperationLogMapper;
import com.forgex.sys.mapper.SysUserPasswordHistoryMapper;
import com.forgex.sys.service.IDataBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 备份与审计归档实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataBackupServiceImpl implements IDataBackupService {

    private final SysDataBackupRecordMapper backupMapper;
    private final SysOperationLogMapper operationLogMapper;
    private final SysUserPasswordHistoryMapper passwordHistoryMapper;

    @Value("${forgex.deployment.backup-dir:./forgex/backup}")
    private String backupDir;

    /**
     * 分页查询备份记录。
     *
     * @param current 页码
     * @param size    页大小
     * @return 分页结果
     */
    @Override
    public Page<SysDataBackupRecord> page(long current, long size) {
        return backupMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<SysDataBackupRecord>().orderByDesc(SysDataBackupRecord::getBackupTime));
    }

    /**
     * 触发全量备份占位记录，实际 dump 由部署脚本/Job 调用操作系统命令。
     *
     * @param operator 操作人
     * @return 记录
     */
    @Override
    public SysDataBackupRecord triggerFullBackup(String operator) {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Path dir = Path.of(backupDir);
        try {
            Files.createDirectories(dir);
        } catch (Exception ex) {
            log.warn("create backup dir failed: {}", ex.getMessage());
        }
        SysDataBackupRecord record = new SysDataBackupRecord();
        record.setBackupName("full-" + stamp);
        record.setBackupType("FULL");
        record.setDatabaseName("forgex_admin,forgex_history,forgex_basic");
        record.setBackupFilePath(dir.resolve("full-" + stamp + ".sql").toString());
        record.setBackupStatus("IN_PROGRESS");
        record.setBackupTime(LocalDateTime.now());
        record.setBackupOperator(operator);
        record.setTenantId(TenantContext.get());
        record.setRemark("triggered by software job/manual");
        backupMapper.insert(record);
        return record;
    }

    /**
     * 归档超过保留期的操作日志。
     *
     * @param retainDays 保留天数
     * @return 归档条数
     */
    @Override
    public int archiveAuditLogs(int retainDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(Math.max(retainDays, 180));
        List<SysOperationLog> expired = operationLogMapper.selectList(new LambdaQueryWrapper<SysOperationLog>()
                .lt(SysOperationLog::getOperationTime, threshold)
                .last("limit 500"));
        if (expired.isEmpty()) {
            trimPasswordHistory();
            return 0;
        }
        try {
            Path archiveDir = Path.of(backupDir, "audit");
            Files.createDirectories(archiveDir);
            StringBuilder raw = new StringBuilder();
            for (SysOperationLog item : expired) {
                raw.append(item.getId()).append('|').append(item.getRecordHash()).append('|')
                        .append(item.getRequestUrl()).append('\n');
            }
            byte[] key = SM4FileEncryptor.generateKey();
            Path plain = archiveDir.resolve("audit-" + System.currentTimeMillis() + ".txt");
            Files.writeString(plain, raw.toString(), StandardCharsets.UTF_8);
            Path cipher = archiveDir.resolve(plain.getFileName() + ".sm4");
            SM4FileEncryptor.encryptFile(plain.toFile(), cipher.toFile(), key);
            String digest = SM3Utils.digestHex(Files.readAllBytes(cipher));
            Files.writeString(archiveDir.resolve(cipher.getFileName() + ".sm3"), digest);
            Files.deleteIfExists(plain);
            for (SysOperationLog item : expired) {
                operationLogMapper.deleteById(item.getId());
            }
        } catch (Exception ex) {
            log.error("archive audit logs failed", ex);
            return 0;
        }
        trimPasswordHistory();
        return expired.size();
    }

    private void trimPasswordHistory() {
        List<SysUserPasswordHistory> all = passwordHistoryMapper.selectList(new LambdaQueryWrapper<SysUserPasswordHistory>()
                .orderByDesc(SysUserPasswordHistory::getCreatedTime));
        all.stream().collect(java.util.stream.Collectors.groupingBy(SysUserPasswordHistory::getUserId))
                .values()
                .forEach(list -> {
                    for (int i = 5; i < list.size(); i++) {
                        passwordHistoryMapper.deleteById(list.get(i).getId());
                    }
                });
    }
}

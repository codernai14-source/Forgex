package com.forgex.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.sys.domain.entity.SysDataBackupRecord;

/**
 * 数据备份服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IDataBackupService {

    /**
     * 分页查询备份记录。
     *
     * @param current 页码
     * @param size    页大小
     * @return 分页结果
     */
    Page<SysDataBackupRecord> page(long current, long size);

    /**
     * 触发一次全量备份并落库。
     *
     * @param operator 操作人
     * @return 记录
     */
    SysDataBackupRecord triggerFullBackup(String operator);

    /**
     * 归档超期审计日志。
     *
     * @param retainDays 保留天数
     * @return 归档条数
     */
    int archiveAuditLogs(int retainDays);
}

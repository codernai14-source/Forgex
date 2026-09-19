package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据备份记录。
 * <p>
 * 映射历史库表 {@code data_backup_record}。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("data_backup_record")
public class SysDataBackupRecord {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 备份名称 */
    private String backupName;

    /** 备份类型 */
    private String backupType;

    /** 数据库名 */
    private String databaseName;

    /** 表名列表 */
    private String tableNames;

    /** 备份文件路径 */
    private String backupFilePath;

    /** 文件大小 */
    private Long backupSize;

    /** 状态 */
    private String backupStatus;

    /** 备份时间 */
    private LocalDateTime backupTime;

    /** 操作人 */
    private String backupOperator;

    /** 备注 */
    private String remark;

    /** 租户 ID */
    private Long tenantId;
}

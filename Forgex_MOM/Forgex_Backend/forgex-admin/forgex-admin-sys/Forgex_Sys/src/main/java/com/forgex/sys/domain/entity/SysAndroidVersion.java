package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 安卓版本管理实体类
 * <p>
 * 对应数据库表 sys_android_version，存储安卓安装包的版本信息、文件地址、更新日志等
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-05
 * @see BaseEntity
 */
@Data
@TableName("sys_android_version")
public class SysAndroidVersion extends BaseEntity {

    /**
     * 版本号（内部递增，如 101）
     */
    @TableField("version_code")
    private Integer versionCode;

    /**
     * 版本名称（展示用，如 1.0.1）
     */
    @TableField("version_name")
    private String versionName;

    /**
     * 更新日志
     */
    @TableField("changelog")
    private String changelog;

    /**
     * 原始文件名（如 app-release.apk）
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件访问 URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 存储类型（LOCAL / OSS / MINIO）
     */
    @TableField("storage_type")
    private String storageType;

    /**
     * 状态：1-启用 0-禁用
     */
    @TableField("status")
    private Integer status;
}

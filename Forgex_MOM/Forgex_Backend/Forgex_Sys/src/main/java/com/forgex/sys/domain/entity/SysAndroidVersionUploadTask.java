package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Android APK chunk upload task.
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-07
 */
@Data
@TableName("sys_android_version_upload_task")
public class SysAndroidVersionUploadTask extends BaseEntity {

    @TableField("upload_id")
    private String uploadId;

    @TableField("file_name")
    private String fileName;

    @TableField("file_size")
    private Long fileSize;

    @TableField("chunk_size")
    private Long chunkSize;

    @TableField("total_chunks")
    private Integer totalChunks;

    @TableField("uploaded_chunks")
    private String uploadedChunks;

    @TableField("uploaded_count")
    private Integer uploadedCount;

    @TableField("status")
    private String status;

    @TableField("file_hash")
    private String fileHash;

    @TableField("temp_dir")
    private String tempDir;

    @TableField("merged_file_path")
    private String mergedFilePath;

    @TableField("final_file_url")
    private String finalFileUrl;

    @TableField("version_id")
    private Long versionId;

    @TableField("error_message")
    private String errorMessage;

    @TableField("version_code")
    private Integer versionCode;

    @TableField("version_name")
    private String versionName;

    @TableField("changelog")
    private String changelog;

    @TableField("expire_time")
    private LocalDateTime expireTime;
}

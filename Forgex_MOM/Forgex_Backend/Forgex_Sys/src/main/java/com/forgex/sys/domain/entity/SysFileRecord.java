package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * File record entity.
 */
@Data
@TableName("sys_file_record")
public class SysFileRecord extends BaseEntity {

    @TableField("module_code")
    private String moduleCode;

    @TableField("module_name")
    private String moduleName;

    @TableField("original_name")
    private String originalName;

    @TableField("stored_name")
    private String storedName;

    @TableField("file_type")
    private String fileType;

    @TableField("content_type")
    private String contentType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("relative_path")
    private String relativePath;

    @TableField("access_url")
    private String accessUrl;

    @TableField("storage_type")
    private String storageType;

    @TableField("storage_config_id")
    private Long storageConfigId;
}

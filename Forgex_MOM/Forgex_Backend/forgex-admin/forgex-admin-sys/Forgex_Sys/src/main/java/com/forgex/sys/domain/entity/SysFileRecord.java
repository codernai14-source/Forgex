package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * File record entity.
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_file_record")
public class SysFileRecord extends BaseEntity {

    /**
     * 模块编码。
     */
    @TableField("module_code")
    private String moduleCode;

    /**
     * 模块名称。
     */
    @TableField("module_name")
    private String moduleName;

    /**
     * original名称。
     */
    @TableField("original_name")
    private String originalName;

    /**
     * stored名称。
     */
    @TableField("stored_name")
    private String storedName;

    /**
     * 文件类型。
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 内容类型。
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 文件size。
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * relative路径。
     */
    @TableField("relative_path")
    private String relativePath;

    /**
     * accessurl。
     */
    @TableField("access_url")
    private String accessUrl;

    /**
     * storage类型。
     */
    @TableField("storage_type")
    private String storageType;

    /**
     * storage配置 ID。
     */
    @TableField("storage_config_id")
    private Long storageConfigId;
}

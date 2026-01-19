package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 文件存储配置实体类
 * <p>存储文件存储的配置信息。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_file_storage")
public class SysFileStorage extends BaseEntity {

    /**
     * 存储类型（LOCAL、OSS等）
     */
    @TableField("storage_type")
    private String storageType;

    /**
     * 存储名称
     */
    @TableField("storage_name")
    private String storageName;

    /**
     * 配置JSON
     */
    @TableField("config_json")
    private String configJson;

    /**
     * 是否为默认存储
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 状态（true-启用，false-禁用）
     */
    @TableField("status")
    private Boolean status;
}


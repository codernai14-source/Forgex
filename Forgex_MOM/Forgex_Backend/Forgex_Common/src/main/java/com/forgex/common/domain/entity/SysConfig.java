package com.forgex.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 系统配置实体
 * 作用：映射数据库表 `sys_config`，用于存储系统各项配置；
 * 字段说明：`configKey` 为配置键；`configValue` 为配置值（建议存放 JSON 文本）。
 */
@Data
@TableName("sys_config")
public class SysConfig extends BaseEntity {
    /** 配置键（唯一标识一个配置项） */
    private String configKey;
    /** 配置值（文本或JSON，推荐JSON以承载复杂结构） */
    private String configValue;
    private String remark;
}

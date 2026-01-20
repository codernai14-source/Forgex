/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.common.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体
 * 作用：映射数据库表 `sys_config`，用于存储系统各项配置；
 * 字段说明：`configKey` 为配置键；`configValue` 为配置值（建议存放 JSON 文本）。
 */
@Data
@TableName("sys_config")
public class SysConfig {
    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /** 租户ID */
    @TableField(value = "tenant_id")
    private Long tenantId;
    
    /** 配置键（唯一标识一个配置项） */
    private String configKey;
    
    /** 配置值（文本或JSON，推荐JSON以承载复杂结构） */
    private String configValue;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /** 逻辑删除：false=未删除 true=已删除 */
    @TableLogic
    @TableField(value = "deleted")
    private Boolean deleted;
    
    /** 备注 */
    private String remark;
}
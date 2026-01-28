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
package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 消息模板主表实体
 * <p>
 * 映射表：sys_message_template
 * 用于配置消息模板的基本信息
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_message_template")
public class SysMessageTemplate extends BaseEntity {
    
    /** 模板编号 */
    private String templateCode;
    
    /** 模板名称 */
    private String templateName;
    
    /** 模板名称多语言JSON */
    private String templateNameI18nJson;
    
    /** 模板版本 */
    private String templateVersion;
    
    /** 消息类型(NOTICE=通知,WARNING=警告,ALARM=报警) */
    private String messageType;
    
    /** 状态(false=禁用,true=启用) */
    private Boolean status;
    
    /** 备注 */
    private String remark;
}


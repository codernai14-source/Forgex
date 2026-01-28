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
 * 消息模板内容表实体
 * <p>
 * 映射表：sys_message_template_content
 * 用于配置消息模板在不同平台的内容
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_message_template_content")
public class SysMessageTemplateContent extends BaseEntity {
    
    /** 模板主表ID */
    private Long templateId;
    
    /** 消息平台(INTERNAL=站内,WECHAT=企业微信,SMS=短信,EMAIL=邮箱) */
    private String platform;
    
    /** 消息标题(支持占位符,如${userName}) */
    private String contentTitle;
    
    /** 消息标题多语言JSON */
    private String contentTitleI18nJson;
    
    /** 消息内容(支持占位符,如${userName}) */
    private String contentBody;
    
    /** 消息内容多语言JSON */
    private String contentBodyI18nJson;
    
    /** 跳转链接 */
    private String linkUrl;
}


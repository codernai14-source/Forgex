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
package com.forgex.common.domain.entity.template;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MQ 消息模板实体
 * <p>
 * 封装 MQ 消息（站内信、邮件、短信）的模板，支持参数替换。
 * 查询优先级：租户配置 → 公共配置（tenant_id=0）
 * </p>
 * <p>
 * 主要字段：
 * <ul>
 *   <li>tenantId - 租户 ID（0 表示公共配置）</li>
 *   <li>templateCode - 模板编码</li>
 *   <li>templateName - 模板名称</li>
 *   <li>messageType - 消息类型：SYSTEM-系统通知，TASK-任务通知，AUDIT-审批通知</li>
 *   <li>messageTitle - 消息标题</li>
 *   <li>messageContent - 消息内容，支持参数占位符</li>
 *   <li>sendChannel - 发送渠道：STATION-站内信，EMAIL-邮件，SMS-短信</li>
 *   <li>params - 参数定义（JSON 格式）</li>
 * </ul>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_mq_message_template")
public class SysMqMessageTemplate extends BaseEntity {
    
    /**
     * 租户 ID
     * <p>0 表示公共配置，其他表示租户级别配置。</p>
     */
    private Long tenantId;
    
    /**
     * 模板编码
     * <p>唯一标识一个模板。</p>
     */
    private String templateCode;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 消息类型
     * <p>SYSTEM-系统通知，TASK-任务通知，AUDIT-审批通知。</p>
     */
    private String messageType;
    
    /**
     * 消息标题
     * <p>支持参数占位符。</p>
     */
    private String messageTitle;
    
    /**
     * 消息内容
     * <p>支持参数占位符。</p>
     */
    private String messageContent;
    
    /**
     * 发送渠道
     * <p>STATION-站内信，EMAIL-邮件，SMS-短信。</p>
     */
    private String sendChannel;
    
    /**
     * 参数定义（JSON 格式）
     */
    private String params;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 备注
     */
    private String remark;
}

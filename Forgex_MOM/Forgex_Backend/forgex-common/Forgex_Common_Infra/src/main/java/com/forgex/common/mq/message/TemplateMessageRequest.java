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
package com.forgex.common.mq.message;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * MQ 模板消息发送请求
 * <p>
 * 用于业务服务向 MQ 发送模板消息请求，包含模板编码、占位符数据、接收人等信息。
 * 支持 CUSTOM 类型接收人动态指定，以及其他类型（ROLE、DEPT、POSITION、USER）从模板配置读取。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * TemplateMessageRequest request = new TemplateMessageRequest();
 * request.setTemplateCode("WF_APPROVED");
 * request.setDataMap(Map.of("userName", "张三", "taskName", "请假申请"));
 * request.setReceiverUserIds(List.of(123L, 456L)); // CUSTOM 类型时传入
 * request.setBizType("APPROVAL");
 * templateMessageSender.sendToMq(request);
 * }</pre>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-08
 * @see TemplateMessageSender
 */
@Data
public class TemplateMessageRequest {

    /**
     * 租户 ID（建议必传）
     * <p>用于 Sys 服务消费时恢复租户上下文，保证多租户数据隔离。</p>
     */
    private Long tenantId;

    /**
     * 发送人用户 ID（可选）
     * <p>用于消息发送人信息展示；为空时由消费端按“系统”处理。</p>
     */
    private Long senderUserId;

    /**
     * 发送人名称（可选）
     * <p>若传入则优先用于消息 senderName 字段。</p>
     */
    private String senderName;
    
    /**
     * 模板编码，不能为空
     * <p>用于查询消息模板配置，例如：WF_APPROVED、WF_PENDING、UNREAD_SUMMARY。</p>
     */
    private String templateCode;
    
    /**
     * 模板内容实体（占位符数据）
     * <p>Key 为占位符名称（不含${}），Value 为实际值。</p>
     * <p>例如：{"userName": "张三", "taskName": "请假申请"}</p>
     */
    private Map<String, Object> dataMap;
    
    /**
     * 接收人 ID 列表（可选）
     * <p>为空则使用模板配置的接收人。</p>
     * <p>适用于 CUSTOM 类型接收人场景，由业务代码动态计算接收人 ID 列表。</p>
     * <p>对于其他类型（ROLE、DEPT、POSITION、USER），此字段被忽略。</p>
     */
    private List<Long> receiverUserIds;
    
    /**
     * 业务类型（可选）
     * <p>用于消息分类和统计，例如：APPROVAL、SYSTEM、WELCOME。</p>
     */
    private String bizType;
    
    /**
     * 扩展字段（可选）
     * <p>用于传递业务特定的额外数据。</p>
     */
    private Map<String, Object> extraFields;
    
    /**
     * 优先级（可选）
     * <p>数字越大优先级越高，默认为 0。</p>
     * <p>用于 MQ 消息优先级队列。</p>
     */
    private Integer priority;
}

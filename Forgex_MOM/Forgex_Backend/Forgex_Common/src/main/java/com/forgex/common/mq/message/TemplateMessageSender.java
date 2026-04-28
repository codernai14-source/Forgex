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

import java.util.Map;

/**
 * MQ 模板消息发送器接口
 * <p>
 * 提供统一的 MQ 消息发送方法，业务服务通过此接口发送模板消息。
 * 消息将被发送到配置的 MQ 队列，由 Sys 服务消费处理。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 方式 1：完整参数
 * TemplateMessageRequest request = new TemplateMessageRequest();
 * request.setTemplateCode("WF_APPROVED");
 * request.setDataMap(Map.of("userName", "张三"));
 * request.setReceiverUserIds(List.of(123L));
 * templateMessageSender.sendToMq(request);
 * 
 * // 方式 2：简化参数（接收人从模板配置读取）
 * templateMessageSender.sendToMq("WF_APPROVED", Map.of("userName", "张三"));
 * }</pre>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-08
 * @see TemplateMessageRequest
 * @see TemplateMessageSenderImpl
 */
public interface TemplateMessageSender {
    
    /**
     * 发送模板消息到 MQ
     * <p>
     * 消息将被发送到配置的 MQ 队列，由 Sys 服务消费。
     * 支持优先级、业务类型等扩展属性。
     * </p>
     *
     * @param request 消息请求参数，不能为空
     * @throws IllegalArgumentException 当 request 为空或 templateCode 为空时抛出
     * @throws I18nBusinessException 当 MQ 发送失败时抛出
     * @see TemplateMessageRequest
     */
    void sendToMq(TemplateMessageRequest request);
    
    /**
     * 发送模板消息到 MQ（简化版）
     * <p>
     * 仅传入模板编码和占位符数据，接收人从模板配置读取。
     * 适用于接收人固定的场景（如 ROLE、DEPT、POSITION、USER 类型）。
     * </p>
     *
     * @param templateCode 模板编码，不能为空
     * @param dataMap 占位符数据，可为空
     * @throws IllegalArgumentException 当 templateCode 为空时抛出
     * @throws I18nBusinessException 当 MQ 发送失败时抛出
     */
    default void sendToMq(String templateCode, Map<String, Object> dataMap) {
        TemplateMessageRequest request = new TemplateMessageRequest();
        request.setTemplateCode(templateCode);
        request.setDataMap(dataMap);
        sendToMq(request);
    }
}

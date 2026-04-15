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
package com.forgex.common.mq.message.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.enums.MessagePromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.mq.message.TemplateMessageRequest;
import com.forgex.common.mq.message.TemplateMessageSender;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * MQ 模板消息发送器实现类（基于 RocketMQ）
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-08
 * @see TemplateMessageSender
 * @see TemplateMessageRequest
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mq.template-message", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TemplateMessageSenderImpl implements TemplateMessageSender {

    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    @Value("${mq.topic.template-message:forgex.message.template.topic}")
    private String topic;

    @Value("${mq.producer.template-message-tag:TEMPLATE_MESSAGE}")
    private String tag;

    @Override
    public void sendToMq(TemplateMessageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("MQ 消息请求不能为空");
        }
        if (!StringUtils.hasText(request.getTemplateCode())) {
            throw new IllegalArgumentException("模板编码不能为空");
        }

        if (request.getTenantId() == null) {
            request.setTenantId(TenantContext.get());
        }
        if (request.getSenderUserId() == null) {
            request.setSenderUserId(UserContext.get());
        }

        if (request.getTenantId() == null) {
            throw new IllegalArgumentException("租户ID不能为空，无法发送模板消息");
        }

        try {
            String messageJson = objectMapper.writeValueAsString(request);
            String destination = StringUtils.hasText(tag) ? topic + ":" + tag : topic;
            rocketMQTemplate.convertAndSend(destination, messageJson);

            log.info("RocketMQ 模板消息发送成功: templateCode={}, tenantId={}, bizType={}, receiverCount={}",
                    request.getTemplateCode(),
                    request.getTenantId(),
                    request.getBizType(),
                    request.getReceiverUserIds() != null ? request.getReceiverUserIds().size() : 0);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("RocketMQ 模板消息发送失败: templateCode={}, tenantId={}, bizType={}",
                    request.getTemplateCode(), request.getTenantId(), request.getBizType(), e);
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, MessagePromptEnum.ROCKETMQ_MSG_SEND_FAILED, e.getMessage());
        }
    }
}

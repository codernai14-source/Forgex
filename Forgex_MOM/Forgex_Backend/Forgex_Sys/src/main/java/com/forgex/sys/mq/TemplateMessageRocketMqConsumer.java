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
package com.forgex.sys.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.mq.message.TemplateMessageRequest;
import com.forgex.sys.service.impl.TemplateMessageServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * RocketMQ 模板消息消费者
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mq.template-message", name = "enabled", havingValue = "true", matchIfMissing = true)
@RocketMQMessageListener(
        topic = "${mq.topic.template-message:forgex.message.template.topic}",
        consumerGroup = "${mq.consumer.template-message-group:forgex-sys-template-message-consumer}",
        selectorExpression = "${mq.consumer.template-message-tag:TEMPLATE_MESSAGE}",
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING
)
public class TemplateMessageRocketMqConsumer implements RocketMQListener<String> {

    private final ObjectMapper objectMapper;
    private final TemplateMessageServiceImpl templateMessageService;

    @Override
    public void onMessage(String messageJson) {
        if (!StringUtils.hasText(messageJson)) {
            log.warn("RocketMQ 模板消息内容为空");
            return;
        }

        try {
            TemplateMessageRequest request = objectMapper.readValue(messageJson, TemplateMessageRequest.class);
            templateMessageService.processTemplateMessageFromMq(request);
        } catch (Exception e) {
            log.error("RocketMQ 模板消息消费失败: message={}", messageJson, e);
        }
    }
}

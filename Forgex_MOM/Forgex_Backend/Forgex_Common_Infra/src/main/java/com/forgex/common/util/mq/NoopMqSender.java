package com.forgex.common.util.mq;

import org.springframework.stereotype.Component;

/**
 * MQ 发送器空实现。
 * <p>
 * 在未接入 MQ 组件时提供默认实现，避免业务代码空指针。\n
 * 后续接入 RocketMQ 后可替换为真实实现。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Component
public class NoopMqSender implements MqSender {

    @Override
    public boolean send(String topic, String tag, String body) {
        return false;
    }
}


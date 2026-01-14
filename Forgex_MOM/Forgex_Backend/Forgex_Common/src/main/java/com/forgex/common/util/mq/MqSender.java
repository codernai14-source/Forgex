package com.forgex.common.util.mq;

/**
 * MQ 发送器抽象。
 * <p>
 * 当前仅定义最小抽象，便于后续统一接入 RocketMQ 等组件。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
public interface MqSender {

    /**
     * 发送消息。
     *
     * @param topic topic
     * @param tag   tag（可为空）
     * @param body  消息体（字符串）
     * @return 是否成功
     */
    boolean send(String topic, String tag, String body);
}


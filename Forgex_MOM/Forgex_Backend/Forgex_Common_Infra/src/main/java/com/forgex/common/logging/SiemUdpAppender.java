package com.forgex.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * 可选 SIEM UDP 上报 Appender。
 * <p>
 * 由环境变量 {@code FORGEX_SIEM_HOST} / {@code FORGEX_SIEM_PORT} 控制；主机为空时静默丢弃。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public class SiemUdpAppender extends AppenderBase<ILoggingEvent> {

    private String host;
    private int port = 514;

    /**
     * 设置目标主机。
     *
     * @param host 主机
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 设置目标端口。
     *
     * @param port 端口
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 发送一条日志。
     *
     * @param event 日志事件
     */
    @Override
    protected void append(ILoggingEvent event) {
        if (host == null || host.isBlank()) {
            return;
        }
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] payload = event.getFormattedMessage().getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(payload, payload.length, InetAddress.getByName(host), port);
            socket.send(packet);
        } catch (Exception ignored) {
            // SIEM 不可用时不影响业务进程
        }
    }
}

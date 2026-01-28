package com.forgex.sys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SSE推送服务（增强版）
 * <p>提供SSE连接管理和消息推送功能，支持：</p>
 * <ul>
 *   <li>心跳保活机制</li>
 *   <li>自动清理过期连接</li>
 *   <li>连接数统计</li>
 *   <li>异常处理和日志记录</li>
 * </ul>
 * 
 * @author Forgex Team
 * @version 2.0.0
 */
@Slf4j
@Service
public class SseEmitterService {

    /**
     * 连接存储：key=tenantId:userId, value=连接列表
     */
    private final Map<String, CopyOnWriteArrayList<SseConnection>> connections = new ConcurrentHashMap<>();

    /**
     * 连接超时时间（毫秒）：30分钟
     */
    private static final long TIMEOUT = 30 * 60 * 1000L;

    /**
     * 心跳间隔（毫秒）：30秒
     */
    private static final long HEARTBEAT_INTERVAL = 30 * 1000L;

    /**
     * 连接计数器
     */
    private final AtomicInteger connectionCounter = new AtomicInteger(0);

    /**
     * SSE连接包装类
     */
    private static class SseConnection {
        SseEmitter emitter;
        LocalDateTime createTime;
        LocalDateTime lastHeartbeat;
        String connectionId;

        SseConnection(SseEmitter emitter, String connectionId) {
            this.emitter = emitter;
            this.connectionId = connectionId;
            this.createTime = LocalDateTime.now();
            this.lastHeartbeat = LocalDateTime.now();
        }
    }

    /**
     * 建立SSE连接
     * 
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return SseEmitter实例
     */
    public SseEmitter connect(Long tenantId, Long userId) {
        String key = buildKey(tenantId, userId);
        String connectionId = generateConnectionId(tenantId, userId);
        
        // 创建SseEmitter，设置超时时间
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        SseConnection connection = new SseConnection(emitter, connectionId);
        
        // 添加到连接池
        connections.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(connection);
        
        // 设置回调
        emitter.onCompletion(() -> {
            log.info("SSE连接完成: connectionId={}, tenantId={}, userId={}", connectionId, tenantId, userId);
            removeConnection(key, connection);
        });
        
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时: connectionId={}, tenantId={}, userId={}", connectionId, tenantId, userId);
            removeConnection(key, connection);
        });
        
        emitter.onError(e -> {
            log.error("SSE连接错误: connectionId={}, tenantId={}, userId={}, error={}", 
                    connectionId, tenantId, userId, e.getMessage());
            removeConnection(key, connection);
        });

        // 发送连接成功消息
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "connectionId", connectionId,
                            "timestamp", System.currentTimeMillis(),
                            "message", "连接成功"
                    )));
            log.info("SSE连接建立成功: connectionId={}, tenantId={}, userId={}, 当前连接数={}", 
                    connectionId, tenantId, userId, getTotalConnections());
        } catch (IOException e) {
            log.error("发送连接成功消息失败: connectionId={}", connectionId, e);
            removeConnection(key, connection);
        }

        return emitter;
    }

    /**
     * 向指定用户发送消息
     * 
     * @param userId 用户ID
     * @param data 消息数据
     */
    public void sendToUser(Long userId, Object data) {
        sendToUser(null, userId, "message", data);
    }

    /**
     * 向指定用户发送消息
     * 
     * @param tenantId 租户ID（可为空，为空则向所有租户的该用户发送）
     * @param userId 用户ID
     * @param event 事件名称
     * @param data 消息数据
     */
    public void sendToUser(Long tenantId, Long userId, String event, Object data) {
        if (userId == null) {
            log.warn("用户ID为空，无法发送消息");
            return;
        }

        // 如果指定了租户ID，只发送给该租户的用户
        if (tenantId != null) {
            String key = buildKey(tenantId, userId);
            sendToConnections(key, event, data);
        } else {
            // 否则发送给所有租户的该用户
            connections.keySet().stream()
                    .filter(key -> key.endsWith(":" + userId))
                    .forEach(key -> sendToConnections(key, event, data));
        }
    }

    /**
     * 向指定连接发送消息
     */
    private void sendToConnections(String key, String event, Object data) {
        List<SseConnection> connectionList = connections.get(key);
        if (connectionList == null || connectionList.isEmpty()) {
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (SseConnection connection : connectionList) {
            try {
                connection.emitter.send(SseEmitter.event().name(event).data(data));
                connection.lastHeartbeat = LocalDateTime.now();
                successCount++;
            } catch (Exception e) {
                log.warn("发送消息失败: connectionId={}, error={}", connection.connectionId, e.getMessage());
                removeConnection(key, connection);
                failCount++;
            }
        }

        if (successCount > 0) {
            log.debug("消息发送完成: key={}, event={}, 成功={}, 失败={}", key, event, successCount, failCount);
        }
    }

    /**
     * 移除连接
     */
    private void removeConnection(String key, SseConnection connection) {
        List<SseConnection> list = connections.get(key);
        if (list == null) {
            return;
        }
        
        list.remove(connection);
        connectionCounter.decrementAndGet();
        
        if (list.isEmpty()) {
            connections.remove(key);
        }
        
        // 完成Emitter
        try {
            connection.emitter.complete();
        } catch (Exception e) {
            log.debug("完成Emitter失败: connectionId={}", connection.connectionId);
        }
    }

    /**
     * 定时发送心跳（每30秒）
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL)
    public void sendHeartbeat() {
        if (connections.isEmpty()) {
            return;
        }

        int totalConnections = 0;
        int heartbeatSuccess = 0;
        int heartbeatFail = 0;

        for (Map.Entry<String, CopyOnWriteArrayList<SseConnection>> entry : connections.entrySet()) {
            String key = entry.getKey();
            List<SseConnection> connectionList = entry.getValue();
            
            for (SseConnection connection : connectionList) {
                totalConnections++;
                try {
                    connection.emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data(Map.of("timestamp", System.currentTimeMillis())));
                    connection.lastHeartbeat = LocalDateTime.now();
                    heartbeatSuccess++;
                } catch (Exception e) {
                    log.warn("心跳发送失败: connectionId={}, error={}", connection.connectionId, e.getMessage());
                    removeConnection(key, connection);
                    heartbeatFail++;
                }
            }
        }

        if (totalConnections > 0) {
            log.debug("心跳发送完成: 总连接数={}, 成功={}, 失败={}", totalConnections, heartbeatSuccess, heartbeatFail);
        }
    }

    /**
     * 定时清理过期连接（每5分钟）
     */
    @Scheduled(fixedRate = 5 * 60 * 1000L)
    public void cleanExpiredConnections() {
        if (connections.isEmpty()) {
            return;
        }

        int cleanedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, CopyOnWriteArrayList<SseConnection>> entry : connections.entrySet()) {
            String key = entry.getKey();
            List<SseConnection> connectionList = entry.getValue();
            
            for (SseConnection connection : connectionList) {
                // 如果超过5分钟没有心跳，认为连接已失效
                if (connection.lastHeartbeat.plusMinutes(5).isBefore(now)) {
                    log.info("清理过期连接: connectionId={}, lastHeartbeat={}", 
                            connection.connectionId, connection.lastHeartbeat);
                    removeConnection(key, connection);
                    cleanedCount++;
                }
            }
        }

        if (cleanedCount > 0) {
            log.info("过期连接清理完成: 清理数量={}, 剩余连接数={}", cleanedCount, getTotalConnections());
        }
    }

    /**
     * 获取当前总连接数
     */
    public int getTotalConnections() {
        return connections.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * 获取指定用户的连接数
     */
    public int getUserConnections(Long tenantId, Long userId) {
        String key = buildKey(tenantId, userId);
        List<SseConnection> list = connections.get(key);
        return list != null ? list.size() : 0;
    }

    /**
     * 断开指定用户的所有连接
     */
    public void disconnectUser(Long tenantId, Long userId) {
        String key = buildKey(tenantId, userId);
        List<SseConnection> list = connections.remove(key);
        if (list != null) {
            for (SseConnection connection : list) {
                try {
                    connection.emitter.complete();
                } catch (Exception e) {
                    log.debug("关闭连接失败: connectionId={}", connection.connectionId);
                }
            }
            log.info("断开用户连接: tenantId={}, userId={}, 连接数={}", tenantId, userId, list.size());
        }
    }

    /**
     * 生成连接Key
     */
    private String buildKey(Long tenantId, Long userId) {
        return tenantId + ":" + userId;
    }

    /**
     * 生成连接ID
     */
    private String generateConnectionId(Long tenantId, Long userId) {
        int count = connectionCounter.incrementAndGet();
        return String.format("SSE-%d-%d-%d-%d", tenantId, userId, System.currentTimeMillis(), count);
    }
}


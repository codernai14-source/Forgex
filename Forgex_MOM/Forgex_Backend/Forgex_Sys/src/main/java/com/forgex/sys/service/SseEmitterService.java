package com.forgex.sys.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseEmitterService {

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> connections = new ConcurrentHashMap<>();

    public SseEmitter connect(Long tenantId, Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        String key = key(tenantId, userId);
        connections.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(key, emitter));
        emitter.onTimeout(() -> remove(key, emitter));
        emitter.onError(e -> remove(key, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException ignored) {
        }

        return emitter;
    }

    public void sendToUser(Long tenantId, Long userId, String event, Object data) {
        String key = key(tenantId, userId);
        List<SseEmitter> emitters = connections.get(key);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(event).data(data));
            } catch (Exception e) {
                remove(key, emitter);
            }
        }
    }

    /**
     * 移除SSE连接
     * 
     * @param key 连接Key
     * @param emitter 要移除的Emitter
     */
    private void remove(String key, SseEmitter emitter) {
        List<SseEmitter> list = connections.get(key);
        if (list == null) {
            return;
        }
        list.remove(emitter);
        if (list.isEmpty()) {
            connections.remove(key);
        }
    }

    /**
     * 生成连接Key
     * 
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 连接Key
     */
    private String key(Long tenantId, Long userId) {
        return String.valueOf(tenantId) + ":" + String.valueOf(userId);
    }
}


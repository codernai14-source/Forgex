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
package com.forgex.sys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SSE（Server-Sent Events）推送服务
 * <p>
 * 提供服务器向客户端推送实时消息的功能，用于租户初始化进度推送等场景。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>创建 SSE 连接</li>
 *   <li>推送消息到指定客户端</li>
 *   <li>推送进度更新</li>
 *   <li>管理活跃的 SSE 连接</li>
 * </ul>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class SsePushService {
    
    /**
     * 存储所有活跃的 SSE 连接
     * key: clientId（用户 ID 或任务 ID）
     */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    /**
     * SSE 心跳线程池
     */
    private final ExecutorService heartbeatExecutor = Executors.newSingleThreadExecutor();
    
    /**
     * 创建 SSE 连接
     * <p>
     * 客户端通过访问 SSE 接口建立长连接，服务端保存 emitter 用于后续推送。
     * </p>
     *
     * @param clientId 客户端标识（用户 ID 或任务 ID）
     * @return SSE 发射器
     */
    public SseEmitter createConnection(String clientId) {
        log.info("创建 SSE 连接：{}", clientId);
        
        // 设置超时时间为 0，表示永不过期（实际通过心跳保持连接）
        SseEmitter emitter = new SseEmitter(0L);
        
        // 保存 emitter
        emitters.put(clientId, emitter);
        
        // 注册回调
        emitter.onCompletion(() -> {
            log.info("SSE 连接完成：{}", clientId);
            emitters.remove(clientId);
        });
        
        emitter.onTimeout(() -> {
            log.info("SSE 连接超时：{}", clientId);
            emitters.remove(clientId);
        });
        
        emitter.onError(e -> {
            log.error("SSE 连接错误：{}", clientId, e);
            emitters.remove(clientId);
        });
        
        // 启动心跳（每 30 秒发送一次心跳，防止连接断开）
        startHeartbeat(clientId, emitter);
        
        return emitter;
    }
    
    /**
     * 推送消息
     * <p>
     * 向指定客户端推送消息。
     * </p>
     *
     * @param clientId 客户端标识
     * @param data 推送数据
     */
    public void push(String clientId, Object data) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(data);
                log.debug("推送 SSE 消息成功：{}, data: {}", clientId, data);
            } catch (IOException e) {
                log.error("推送 SSE 消息失败：{}", clientId, e);
                emitter.completeWithError(e);
                emitters.remove(clientId);
            }
        } else {
            log.warn("未找到 SSE 连接：{}", clientId);
        }
    }
    
    /**
     * 推送进度更新
     * <p>
     * 向指定客户端推送任务进度。
     * </p>
     *
     * @param taskId 任务 ID
     * @param progress 进度百分比（0-100）
     * @param currentStep 当前步骤描述
     */
    public void pushProgress(String taskId, Integer progress, String currentStep) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("taskId", taskId);
        data.put("progress", progress);
        data.put("currentStep", currentStep);
        data.put("timestamp", System.currentTimeMillis());
        
        push(taskId, data);
    }
    
    /**
     * 推送完成消息
     *
     * @param taskId 任务 ID
     * @param success 是否成功
     * @param message 消息内容
     */
    public void pushComplete(String taskId, boolean success, String message) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("taskId", taskId);
        data.put("progress", 100);
        data.put("status", success ? "SUCCESS" : "FAILED");
        data.put("message", message);
        data.put("timestamp", System.currentTimeMillis());
        
        push(taskId, data);
    }
    
    /**
     * 关闭连接
     * <p>
     * 主动关闭指定客户端的 SSE 连接。
     * </p>
     *
     * @param clientId 客户端标识
     */
    public void closeConnection(String clientId) {
        SseEmitter emitter = emitters.remove(clientId);
        if (emitter != null) {
            emitter.complete();
            log.info("关闭 SSE 连接：{}", clientId);
        }
    }
    
    /**
     * 获取活跃连接数
     *
     * @return 活跃连接数量
     */
    public int getActiveConnectionCount() {
        return emitters.size();
    }
    
    /**
     * 启动心跳
     * <p>
     * 每 30 秒发送一次心跳消息，保持连接活跃。
     * </p>
     *
     * @param clientId 客户端标识
     * @param emitter SSE 发射器
     */
    private void startHeartbeat(String clientId, SseEmitter emitter) {
        heartbeatExecutor.execute(() -> {
            try {
                while (emitters.containsKey(clientId)) {
                    Thread.sleep(30000); // 30 秒
                    
                    if (emitters.containsKey(clientId)) {
                        // 发送心跳
                        Map<String, Object> heartbeat = new ConcurrentHashMap<>();
                        heartbeat.put("type", "heartbeat");
                        heartbeat.put("timestamp", System.currentTimeMillis());
                        emitter.send(heartbeat);
                        log.debug("发送 SSE 心跳：{}", clientId);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug("心跳线程中断：{}", clientId);
            } catch (IOException e) {
                log.debug("心跳发送失败，连接已断开：{}", clientId);
                emitters.remove(clientId);
            }
        });
    }
}

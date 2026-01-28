package com.forgex.sys.controller;

import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.sys.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 系统消息SSE控制器
 * <p>提供SSE连接接口，用于实时推送系统消息。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/message")
@RequiredArgsConstructor
public class SysMessageSseController {

    /**
     * SSE推送服务
     */
    private final SseEmitterService sseEmitterService;

    /**
     * 建立SSE连接
     * <p>为当前用户建立SSE连接，用于接收实时消息推送。</p>
     * 
     * @return SSEEmitter实例，未登录返回立即关闭的Emitter
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        // 获取当前租户ID和用户ID
        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();
        
        // 未登录则返回立即关闭的Emitter
        if (tenantId == null || userId == null) {
            return new SseEmitter(0L);
        }
        
        // 建立SSE连接
        return sseEmitterService.connect(tenantId, userId);
    }
}


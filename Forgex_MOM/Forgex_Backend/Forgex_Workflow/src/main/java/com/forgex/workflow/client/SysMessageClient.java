package com.forgex.workflow.client;

import com.forgex.common.web.R;
import com.forgex.workflow.client.dto.SysMessageSendRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 系统消息 Feign 客户端。
 * <p>
 * 工作流模块通过该客户端统一调用消息中心发送站内消息，
 * 由系统服务负责落库并通过 SSE 推送实时通知。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-06
 */
@FeignClient(name = "forgex-sys", contextId = "sysMessageClient")
public interface SysMessageClient {

    /**
     * 发送站内消息。
     *
     * @param request 消息发送请求
     * @return 消息 ID
     */
    @PostMapping("/sys/message/send")
    R<Long> send(@RequestBody SysMessageSendRequest request);
}

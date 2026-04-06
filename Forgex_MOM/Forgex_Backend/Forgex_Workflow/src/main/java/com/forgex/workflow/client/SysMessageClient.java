package com.forgex.workflow.client;

import com.forgex.common.web.R;
import com.forgex.workflow.client.dto.SysMessageSendRequest;
import com.forgex.workflow.client.dto.TemplateMessageSendRequest;
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

    /**
     * 使用模板发送消息。
     * <p>
     * 根据模板编码和占位符数据发送消息，支持批量接收人。
     * </p>
     *
     * @param request 模板消息发送请求
     * @return 发送成功的消息数量
     */
    @PostMapping("/sys/message/send-by-template")
    R<Integer> sendByTemplate(@RequestBody TemplateMessageSendRequest request);

    /**
     * 使用模板发送消息给单个用户。
     *
     * @param request 模板消息发送请求
     * @return 消息 ID
     */
    @PostMapping("/sys/message/send-to-user")
    R<Long> sendToUser(@RequestBody TemplateMessageSendRequest request);
}

package com.forgex.sys.api.feign;

import com.forgex.common.web.R;
import com.forgex.sys.api.dto.TemplateMessageSendRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 系统消息模板能力的跨服务调用契约。
 */
@FeignClient(name = "forgex-sys", contextId = "sysTemplateMessageFeignClient", path = "/sys/message")
public interface SysTemplateMessageFeignClient {

    /**
     * 使用模板向多个用户发送消息。
     *
     * @param request 发送请求
     * @return 成功发送数量
     */
    @PostMapping("/send-by-template")
    R<Integer> sendByTemplate(@RequestBody TemplateMessageSendRequestDTO request);

    /**
     * 使用模板向单个用户发送消息。
     *
     * @param request 发送请求
     * @return 消息 ID
     */
    @PostMapping("/send-to-user")
    R<Long> sendToUser(@RequestBody TemplateMessageSendRequestDTO request);

    /**
     * 检查模板是否可用。
     *
     * @param templateCode 模板编码
     * @return 模板是否可用
     */
    @GetMapping("/template-available")
    R<Boolean> isTemplateAvailable(@RequestParam("templateCode") String templateCode);
}

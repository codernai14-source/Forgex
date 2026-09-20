package com.forgex.admin.client;

import com.forgex.common.service.TemplateMessageService;
import com.forgex.common.web.R;
import com.forgex.sys.api.dto.TemplateMessageSendRequestDTO;
import com.forgex.sys.api.feign.SysTemplateMessageFeignClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 通过 Sys 服务发送模板消息的远程适配器。
 */
public class RemoteTemplateMessageService implements TemplateMessageService {

    private static final int SUCCESS_CODE = 200;

    private final SysTemplateMessageFeignClient client;

    public RemoteTemplateMessageService(SysTemplateMessageFeignClient client) {
        this.client = client;
    }

    @Override
    public int sendByTemplate(String templateCode, List<Long> receiverUserIds,
                             Map<String, Object> dataMap, String bizType) {
        R<Integer> response = client.sendByTemplate(
                new TemplateMessageSendRequestDTO(templateCode, receiverUserIds, null, dataMap, bizType));
        return successful(response) ? Objects.requireNonNullElse(response.getData(), 0) : 0;
    }

    @Override
    public int sendByTemplate(String templateCode, Map<String, Object> dataMap) {
        return sendByTemplate(templateCode, null, dataMap, null);
    }

    @Override
    public Long sendToUser(String templateCode, Long receiverUserId,
                           Map<String, Object> dataMap, String bizType) {
        R<Long> response = client.sendToUser(
                new TemplateMessageSendRequestDTO(templateCode, null, receiverUserId, dataMap, bizType));
        return successful(response) ? response.getData() : null;
    }

    @Override
    public boolean isTemplateAvailable(String templateCode) {
        R<Boolean> response = client.isTemplateAvailable(templateCode);
        return successful(response) && Boolean.TRUE.equals(response.getData());
    }

    private boolean successful(R<?> response) {
        return response != null && Integer.valueOf(SUCCESS_CODE).equals(response.getCode());
    }
}

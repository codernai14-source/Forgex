package com.forgex.sys.api.dto;

import java.util.List;
import java.util.Map;

/**
 * 模板消息发送请求。
 *
 * @param templateCode 模板编码
 * @param receiverUserIds 接收人用户 ID 列表
 * @param receiverUserId 单个接收人用户 ID
 * @param dataMap 模板占位符数据
 * @param bizType 业务类型
 */
public record TemplateMessageSendRequestDTO(
        String templateCode,
        List<Long> receiverUserIds,
        Long receiverUserId,
        Map<String, Object> dataMap,
        String bizType) {
}

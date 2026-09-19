package com.forgex.workflow.client.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 模板消息发送请求DTO。
 * <p>
 * 工作流模块调用模板消息发送接口的请求参数。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-06
 */
@Data
public class TemplateMessageSendRequest {

    /**
     * 模板编码（必填）。
     */
    private String templateCode;

    /**
     * 接收人用户ID列表（必填）。
     */
    private List<Long> receiverUserIds;

    /**
     * 单个接收人用户ID（可选）。
     */
    private Long receiverUserId;

    /**
     * 占位符数据Map（可选）。
     */
    private Map<String, Object> dataMap;

    /**
     * 业务类型（可选）。
     */
    private String bizType;
}
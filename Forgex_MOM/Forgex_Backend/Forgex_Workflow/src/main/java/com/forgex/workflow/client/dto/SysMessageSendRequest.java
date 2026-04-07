package com.forgex.workflow.client.dto;

import lombok.Data;

/**
 * 工作流调用系统消息发送接口的请求参数。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-06
 */
@Data
public class SysMessageSendRequest {

    /**
     * 接收方租户 ID。
     */
    private Long receiverTenantId;

    /**
     * 接收方用户 ID。
     */
    private Long receiverUserId;

    /**
     * 消息范围。
     */
    private String scope;

    /**
     * 消息类型。
     */
    private String messageType;

    /**
     * 消息渠道。
     */
    private String platform;

    /**
     * 消息标题。
     */
    private String title;

    /**
     * 消息内容。
     */
    private String content;

    /**
     * 跳转链接。
     */
    private String linkUrl;

    /**
     * 业务类型。
     */
    private String bizType;
}

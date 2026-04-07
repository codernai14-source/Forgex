package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 消息发送DTO
 * <p>封装发送系统消息所需的参数。</p>
 * 
 * @author Forgex Team
 * @version 1.0.1
 */
@Data
public class SysMessageSendDTO {
    /**
     * 接收方租户ID
     */
    private Long receiverTenantId;

    /**
     * 接收方用户ID
     */
    private Long receiverUserId;

    /**
     * 消息范围（INTERNAL-内部消息，EXTERNAL-外部消息）
     */
    private String scope;

    /**
     * 消息类型：NOTICE=通知，WARNING=警告，ALARM=报警（对应表字段 {@code message_type}）。
     * <p>未传时由服务端默认 {@code NOTICE}。</p>
     */
    private String messageType;

    /**
     * 消息渠道：INTERNAL=站内，WECHAT=企业微信，SMS=短信，EMAIL=邮箱（对应表字段 {@code platform}）。
     * <p>未传时由服务端默认 {@code INTERNAL}（站内）。</p>
     */
    private String platform;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 链接URL
     */
    private String linkUrl;

    /**
     * 业务类型
     */
    private String bizType;
}


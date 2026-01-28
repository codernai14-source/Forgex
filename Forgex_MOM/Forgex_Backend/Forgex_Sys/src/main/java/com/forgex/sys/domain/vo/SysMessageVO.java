package com.forgex.sys.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统消息VO
 * <p>用于前端展示的系统消息信息。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class SysMessageVO {
    /**
     * 消息ID
     */
    private Long id;

    /**
     * 发送方租户ID
     */
    private Long senderTenantId;

    /**
     * 发送方用户ID
     */
    private Long senderUserId;

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

    /**
     * 状态（0-未读，1-已读）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime readTime;
}


package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统消息实体类
 * <p>存储系统消息的发送和接收信息。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_message")
public class SysMessage extends BaseEntity {

    /**
     * 发送方租户ID
     */
    @TableField("sender_tenant_id")
    private Long senderTenantId;

    /**
     * 发送方用户ID
     */
    @TableField("sender_user_id")
    private Long senderUserId;

    /**
     * 接收方用户ID
     */
    @TableField("receiver_user_id")
    private Long receiverUserId;

    /**
     * 消息范围
     */
    @TableField("scope")
    private String scope;

    /**
     * 消息标题
     */
    @TableField("title")
    private String title;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 链接URL
     */
    @TableField("link_url")
    private String linkUrl;

    /**
     * 业务类型
     */
    @TableField("biz_type")
    private String bizType;

    /**
     * 状态（0-未读，1-已读）
     */
    @TableField("status")
    private Integer status;

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("read_time")
    private LocalDateTime readTime;
}


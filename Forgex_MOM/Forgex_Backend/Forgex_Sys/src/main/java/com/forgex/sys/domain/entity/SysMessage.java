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
     * 发送人名称(格式:姓名+账号,如张三(admin),系统消息则为系统(admin))
     */
    @TableField("sender_name")
    private String senderName;

    /**
     * 接收方用户ID
     */
    @TableField("receiver_user_id")
    private Long receiverUserId;

    /**
     * 消息范围（INTERNAL-内部消息，EXTERNAL-外部消息）
     */
    @TableField("scope")
    private String scope;

    /**
     * 模板编号
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 消息类型(NOTICE=通知,WARNING=警告,ALARM=报警)
     */
    @TableField("message_type")
    private String messageType;

    /**
     * 消息平台(INTERNAL=站内,WECHAT=企业微信,SMS=短信,EMAIL=邮箱)
     */
    @TableField("platform")
    private String platform;

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

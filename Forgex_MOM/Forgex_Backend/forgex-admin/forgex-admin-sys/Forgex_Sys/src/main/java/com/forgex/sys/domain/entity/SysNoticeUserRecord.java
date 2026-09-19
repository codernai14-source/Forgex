package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统通知用户弹出记录实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice_user_record")
public class SysNoticeUserRecord extends BaseEntity {

    /** 通知 ID。 */
    @TableField("notice_id")
    private Long noticeId;

    /** 用户 ID。 */
    @TableField("user_id")
    private Long userId;

    /** 弹出时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("popup_time")
    private LocalDateTime popupTime;

    /** 确认时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("ack_time")
    private LocalDateTime ackTime;
}

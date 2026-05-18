package com.forgex.basic.workcalendar.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工作日历个人日程。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_work_calendar_user_event")
public class BasicWorkCalendarUserEvent extends BaseEntity {

    @TableField("owner_user_id")
    private Long ownerUserId;

    @TableField("record_type")
    private String recordType;

    @TableField("event_title")
    private String eventTitle;

    @TableField("event_content")
    private String eventContent;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("notify_user_ids")
    private String notifyUserIds;

    @TableField("remind_minutes")
    private Integer remindMinutes;

    @TableField("remind_time")
    private LocalDateTime remindTime;

    @TableField("message_template_code")
    private String messageTemplateCode;

    @TableField("remark")
    private String remark;
}

package com.forgex.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 日历提醒任务清单。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_calendar_reminder_task")
public class SysCalendarReminderTask extends BaseEntity {
    private String sourceType;
    private Long sourceId;
    private Long ownerUserId;
    private String title;
    private String recordType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime remindTime;
    private String notifyUserIds;
    private String templateCode;
    private String templateData;
    private Integer status;
    private Integer sendCount;
    private Integer maxRetryCount;
    private LocalDateTime nextRetryTime;
    private LocalDateTime sentTime;
    private String failReason;
}

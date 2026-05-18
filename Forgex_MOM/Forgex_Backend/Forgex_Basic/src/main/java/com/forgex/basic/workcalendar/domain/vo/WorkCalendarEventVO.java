package com.forgex.basic.workcalendar.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作日历日程视图对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class WorkCalendarEventVO {
    private Long id;
    private String scope;
    private String recordType;
    private String eventTitle;
    private String eventContent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Long> notifyUserIds;
    private Integer remindMinutes;
    private LocalDateTime remindTime;
    private String messageTemplateCode;
    private Long ownerUserId;
    private Long sourceUserId;
    private String remark;
}

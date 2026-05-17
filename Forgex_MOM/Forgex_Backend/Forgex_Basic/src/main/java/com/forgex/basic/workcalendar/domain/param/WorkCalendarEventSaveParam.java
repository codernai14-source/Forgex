package com.forgex.basic.workcalendar.domain.param;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作日历日程保存参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class WorkCalendarEventSaveParam {
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
    private String remark;
}

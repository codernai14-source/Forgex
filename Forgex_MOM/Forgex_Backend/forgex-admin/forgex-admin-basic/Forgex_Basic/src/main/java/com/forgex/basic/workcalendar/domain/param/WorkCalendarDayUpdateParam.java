package com.forgex.basic.workcalendar.domain.param;

import lombok.Data;

import java.time.LocalDate;

/**
 * 工作日历日期编辑参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class WorkCalendarDayUpdateParam {
    private LocalDate calendarDate;
    private Integer dateType;
    private String holidayName;
    private String customWeek;
    private String publicWeek;
    private String remark;
}

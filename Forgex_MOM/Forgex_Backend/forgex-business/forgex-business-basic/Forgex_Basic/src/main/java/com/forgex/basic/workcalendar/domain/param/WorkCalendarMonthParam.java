package com.forgex.basic.workcalendar.domain.param;

import lombok.Data;

import java.util.List;

/**
 * 工作日历月份查询参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class WorkCalendarMonthParam {
    private Integer year;
    private Integer month;
    private Boolean syncHoliday;
    private List<String> calendarScopes;
}

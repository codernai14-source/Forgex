package com.forgex.basic.workcalendar.domain.vo;

import com.forgex.basic.workcalendar.domain.entity.BasicWorkCalendarDay;
import lombok.Data;

import java.util.List;

/**
 * 工作日历月份视图对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class WorkCalendarMonthVO {
    private Integer year;
    private Integer month;
    private Boolean syncHoliday;
    private List<BasicWorkCalendarDay> days;
    private List<WorkCalendarEventVO> events;
}

package com.forgex.basic.workcalendar.service;

import com.forgex.basic.workcalendar.domain.param.WorkCalendarDayUpdateParam;
import com.forgex.basic.workcalendar.domain.param.WorkCalendarEventDeleteParam;
import com.forgex.basic.workcalendar.domain.param.WorkCalendarEventSaveParam;
import com.forgex.basic.workcalendar.domain.param.WorkCalendarMonthParam;
import com.forgex.basic.workcalendar.domain.vo.WorkCalendarMonthVO;

/**
 * 工作日历服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
public interface IWorkCalendarService {

    WorkCalendarMonthVO month(WorkCalendarMonthParam param);

    Long saveEvent(WorkCalendarEventSaveParam param);

    Boolean deleteEvent(WorkCalendarEventDeleteParam param);

    Boolean updateDay(WorkCalendarDayUpdateParam param);
}

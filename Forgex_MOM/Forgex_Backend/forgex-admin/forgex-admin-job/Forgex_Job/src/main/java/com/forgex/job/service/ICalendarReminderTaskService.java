package com.forgex.job.service;

import com.forgex.common.api.dto.CalendarReminderCancelDTO;
import com.forgex.common.api.dto.CalendarReminderTaskSyncDTO;

/**
 * 日历提醒任务服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
public interface ICalendarReminderTaskService {

    Boolean sync(CalendarReminderTaskSyncDTO param);

    Boolean cancel(CalendarReminderCancelDTO param);

    void scanDueTasks();
}

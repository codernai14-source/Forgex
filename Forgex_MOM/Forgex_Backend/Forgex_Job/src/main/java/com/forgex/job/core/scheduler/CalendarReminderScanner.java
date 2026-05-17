package com.forgex.job.core.scheduler;

import com.forgex.job.service.ICalendarReminderTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 工作日历提醒扫描器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Component
@RequiredArgsConstructor
public class CalendarReminderScanner {

    private final ICalendarReminderTaskService calendarReminderTaskService;

    @Scheduled(fixedDelayString = "${forgex.job.calendar-reminder.scan-interval-ms:30000}")
    public void scan() {
        calendarReminderTaskService.scanDueTasks();
    }
}

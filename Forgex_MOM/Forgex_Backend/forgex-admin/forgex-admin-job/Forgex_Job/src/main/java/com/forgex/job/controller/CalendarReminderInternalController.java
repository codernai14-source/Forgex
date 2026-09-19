package com.forgex.job.controller;

import com.forgex.common.api.dto.CalendarReminderCancelDTO;
import com.forgex.common.api.dto.CalendarReminderTaskSyncDTO;
import com.forgex.common.web.R;
import com.forgex.job.service.ICalendarReminderTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日历提醒内部接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@RestController
@RequestMapping("/calendar-reminder/internal")
@RequiredArgsConstructor
public class CalendarReminderInternalController {

    private final ICalendarReminderTaskService calendarReminderTaskService;

    @PostMapping("/sync")
    public R<Boolean> sync(@RequestBody CalendarReminderTaskSyncDTO param) {
        return R.ok(calendarReminderTaskService.sync(param));
    }

    @PostMapping("/cancel")
    public R<Boolean> cancel(@RequestBody CalendarReminderCancelDTO param) {
        return R.ok(calendarReminderTaskService.cancel(param));
    }
}

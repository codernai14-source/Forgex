package com.forgex.common.api.feign;

import com.forgex.common.api.dto.CalendarReminderCancelDTO;
import com.forgex.common.api.dto.CalendarReminderTaskSyncDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 工作日历提醒任务内部 Feign 客户端。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@FeignClient(name = "forgex-job", contextId = "calendarReminderFeignClient", path = "/job/calendar-reminder/internal")
public interface CalendarReminderFeignClient {

    /**
     * 同步日程提醒任务。
     *
     * @param param 同步参数
     * @return 处理结果
     */
    @PostMapping("/sync")
    R<Boolean> sync(@RequestBody CalendarReminderTaskSyncDTO param);

    /**
     * 取消日程未发送提醒任务。
     *
     * @param param 取消参数
     * @return 处理结果
     */
    @PostMapping("/cancel")
    R<Boolean> cancel(@RequestBody CalendarReminderCancelDTO param);
}

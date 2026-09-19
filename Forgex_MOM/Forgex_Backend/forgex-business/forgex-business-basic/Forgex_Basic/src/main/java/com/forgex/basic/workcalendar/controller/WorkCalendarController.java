package com.forgex.basic.workcalendar.controller;

import com.forgex.basic.workcalendar.domain.param.WorkCalendarDayUpdateParam;
import com.forgex.basic.workcalendar.domain.param.WorkCalendarEventDeleteParam;
import com.forgex.basic.workcalendar.domain.param.WorkCalendarEventSaveParam;
import com.forgex.basic.workcalendar.domain.param.WorkCalendarMonthParam;
import com.forgex.basic.workcalendar.domain.vo.WorkCalendarMonthVO;
import com.forgex.basic.workcalendar.service.IWorkCalendarService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作日历控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@RestController
@RequestMapping("/work-calendar")
@RequiredArgsConstructor
public class WorkCalendarController {

    private final IWorkCalendarService workCalendarService;

    @RequirePerm("basic:workCalendar:query")
    @PostMapping("/month")
    public R<WorkCalendarMonthVO> month(@RequestBody(required = false) WorkCalendarMonthParam param) {
        return R.ok(workCalendarService.month(param));
    }

    @RequirePerm({"basic:workCalendar:add", "basic:workCalendar:edit"})
    @PostMapping("/event/save")
    public R<Long> saveEvent(@RequestBody WorkCalendarEventSaveParam param) {
        param.setScope("USER");
        return R.ok(CommonPrompt.SAVE_SUCCESS, workCalendarService.saveEvent(param));
    }

    @RequirePerm("basic:workCalendar:delete")
    @PostMapping("/event/delete")
    public R<Boolean> deleteEvent(@RequestBody WorkCalendarEventDeleteParam param) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, workCalendarService.deleteEvent(param));
    }

    @RequirePerm("basic:workCalendar:edit")
    @PostMapping("/day/update")
    public R<Boolean> updateDay(@RequestBody WorkCalendarDayUpdateParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, workCalendarService.updateDay(param));
    }

    @RequirePerm("basic:workCalendar:pushTenant")
    @PostMapping("/event/push-tenant")
    public R<Long> pushTenant(@RequestBody WorkCalendarEventSaveParam param) {
        param.setScope("TENANT");
        return R.ok(CommonPrompt.SAVE_SUCCESS, workCalendarService.saveEvent(param));
    }
}

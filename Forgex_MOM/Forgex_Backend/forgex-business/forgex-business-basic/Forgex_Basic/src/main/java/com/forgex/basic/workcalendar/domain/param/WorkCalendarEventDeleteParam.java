package com.forgex.basic.workcalendar.domain.param;

import lombok.Data;

/**
 * 工作日历日程删除参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class WorkCalendarEventDeleteParam {
    private Long id;
    private String scope;
}

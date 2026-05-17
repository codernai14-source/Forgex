package com.forgex.common.api.dto;

import lombok.Data;

/**
 * 工作日历提醒取消参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class CalendarReminderCancelDTO {

    /** 来源类型：USER_EVENT、TENANT_EVENT。 */
    private String sourceType;

    /** 来源日程 ID。 */
    private Long sourceId;

    /** 租户 ID。 */
    private Long tenantId;
}

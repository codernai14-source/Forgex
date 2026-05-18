package com.forgex.common.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工作日历提醒任务同步参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class CalendarReminderTaskSyncDTO {

    /** 来源类型：USER_EVENT、TENANT_EVENT。 */
    private String sourceType;

    /** 来源日程 ID。 */
    private Long sourceId;

    /** 租户 ID。 */
    private Long tenantId;

    /** 日程创建人或拥有者 ID。 */
    private Long ownerUserId;

    /** 日程标题。 */
    private String title;

    /** 记录类型。 */
    private String recordType;

    /** 开始时间。 */
    private LocalDateTime startTime;

    /** 结束时间。 */
    private LocalDateTime endTime;

    /** 提醒时间。 */
    private LocalDateTime remindTime;

    /** 通知人 ID 集合。 */
    private List<Long> notifyUserIds;

    /** 消息模板编码。 */
    private String templateCode;

    /** 模板变量。 */
    private Map<String, Object> templateData;
}

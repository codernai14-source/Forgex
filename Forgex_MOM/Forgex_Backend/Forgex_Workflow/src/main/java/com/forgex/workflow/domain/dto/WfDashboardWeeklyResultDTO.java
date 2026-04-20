package com.forgex.workflow.domain.dto;

import lombok.Data;

/**
 * 审批工作台近 7 日审批结果趋势项。
 *
 * @author Forgex Team
 * @since 2026-04-10
 */
@Data
public class WfDashboardWeeklyResultDTO {

    /**
     * 日期，格式 yyyy-MM-dd。
     */
    private String date;

    /**
     * 当日审批通过数量。
     */
    private Long approvedCount;

    /**
     * 当日驳回数量。
     */
    private Long rejectedCount;
}

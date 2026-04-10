package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 审批工作台图表统计数据。
 * <p>
 * 用于审批首页图表区域，包含近 7 日审批结果趋势与发起人审批占比。
 * </p>
 *
 * @author Forgex Team
 * @since 2026-04-10
 */
@Data
public class WfDashboardAnalyticsVO {

    /**
     * 近 7 日审批结果趋势。
     */
    private List<WfDashboardWeeklyResultDTO> weeklyResults;

    /**
     * 发起人审批数量占比。
     */
    private List<WfDashboardUserShareDTO> userShares;
}

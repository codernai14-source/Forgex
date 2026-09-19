package com.forgex.integration.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 接口平台首页概览数据。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class IntegrationDashboardOverviewVO {

    /**
     * 汇总指标。
     */
    private IntegrationDashboardSummaryVO summary;

    /**
     * 入站/出站接口数量。
     */
    private List<IntegrationDashboardChartItemVO> directionStats;

    /**
     * 成功/失败调用对比。
     */
    private List<IntegrationDashboardChartItemVO> statusComparison;

    /**
     * 调用状态占比。
     */
    private List<IntegrationDashboardChartItemVO> statusPie;

    /**
     * 近 14 天调用趋势。
     */
    private List<IntegrationDashboardTrendItemVO> callTrend;

    /**
     * 高频接口排行。
     */
    private List<IntegrationDashboardTopApiVO> topApis;

    /**
     * 最近失败调用。
     */
    private List<IntegrationDashboardFailureVO> recentFailures;
}

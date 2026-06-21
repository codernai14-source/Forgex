package com.forgex.integration.domain.vo;

import lombok.Data;

/**
 * 接口平台首页汇总指标。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class IntegrationDashboardSummaryVO {

    /**
     * 第三方系统总数。
     */
    private Long totalThirdSystems;

    /**
     * 启用第三方系统数。
     */
    private Long enabledThirdSystems;

    /**
     * 接口配置总数。
     */
    private Long totalApis;

    /**
     * 启用接口数。
     */
    private Long enabledApis;

    /**
     * 入站接口数，INBOUND 表示外调内。
     */
    private Long inboundApis;

    /**
     * 出站接口数，OUTBOUND 表示内调外。
     */
    private Long outboundApis;

    /**
     * 今日调用次数。
     */
    private Long todayCalls;

    /**
     * 统计周期内调用总数。
     */
    private Long totalCalls;

    /**
     * 成功调用次数。
     */
    private Long successCalls;

    /**
     * 失败调用次数。
     */
    private Long failCalls;

    /**
     * 成功率，单位：百分比。
     */
    private Double successRate;
}

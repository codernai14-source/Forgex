package com.forgex.integration.domain.vo;

import lombok.Data;

/**
 * 首页高频接口排行项。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class IntegrationDashboardTopApiVO {

    /**
     * 接口配置 ID。
     */
    private Long apiConfigId;

    /**
     * 接口编码。
     */
    private String apiCode;

    /**
     * 接口名称。
     */
    private String apiName;

    /**
     * 调用方向，INBOUND-外调内，OUTBOUND-内调外。
     */
    private String callDirection;

    /**
     * 调用总数。
     */
    private Long totalCalls;

    /**
     * 成功调用数。
     */
    private Long successCalls;

    /**
     * 失败调用数。
     */
    private Long failCalls;

    /**
     * 成功率，单位：百分比。
     */
    private Double successRate;
}

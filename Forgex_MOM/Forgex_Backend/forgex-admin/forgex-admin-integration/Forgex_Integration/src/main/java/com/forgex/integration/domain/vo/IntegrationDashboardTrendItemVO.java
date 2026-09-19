package com.forgex.integration.domain.vo;

import lombok.Data;

/**
 * 首页调用趋势项。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class IntegrationDashboardTrendItemVO {

    /**
     * 日期，格式 yyyy-MM-dd。
     */
    private String date;

    /**
     * 调用总数。
     */
    private Long total;

    /**
     * 成功调用数。
     */
    private Long success;

    /**
     * 失败调用数。
     */
    private Long fail;
}

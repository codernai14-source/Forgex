package com.forgex.integration.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首页图表通用键值项。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationDashboardChartItemVO {

    /**
     * 维度名称。
     */
    private String name;

    /**
     * 统计值。
     */
    private Long value;
}

package com.forgex.integration.service;

import com.forgex.integration.domain.vo.IntegrationDashboardOverviewVO;

/**
 * 接口平台首页统计服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IIntegrationDashboardService {

    /**
     * 查询接口平台首页概览数据。
     *
     * @return 首页概览数据
     */
    IntegrationDashboardOverviewVO getOverview();
}

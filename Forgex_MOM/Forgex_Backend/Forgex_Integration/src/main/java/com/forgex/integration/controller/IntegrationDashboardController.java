package com.forgex.integration.controller;

import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.integration.domain.vo.IntegrationDashboardOverviewVO;
import com.forgex.integration.service.IIntegrationDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口平台首页统计控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "接口平台首页", description = "接口平台首页工作台统计")
public class IntegrationDashboardController {

    private final IIntegrationDashboardService integrationDashboardService;

    /**
     * 查询接口平台首页概览。
     *
     * @return 首页概览数据
     */
    @RequirePerm("integration:home:view")
    @GetMapping("/overview")
    @Operation(summary = "查询接口平台首页概览")
    public R<IntegrationDashboardOverviewVO> getOverview() {
        return R.ok(integrationDashboardService.getOverview());
    }
}

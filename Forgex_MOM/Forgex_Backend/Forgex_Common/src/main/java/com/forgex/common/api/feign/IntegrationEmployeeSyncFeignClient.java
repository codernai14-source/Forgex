package com.forgex.common.api.feign;

import com.forgex.common.api.dto.EmployeeThirdPartySyncDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncResultDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 人员第三方同步 Feign 客户端。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@FeignClient(name = "forgex-basic", contextId = "integrationEmployeeSyncFeignClient", path = "/basic/employee/internal")
public interface IntegrationEmployeeSyncFeignClient {

    /**
     * 同步第三方人员数据到基础数据模块。
     *
     * @param request 同步请求
     * @return 同步结果
     */
    @PostMapping("/sync-third-party-employees")
    R<EmployeeThirdPartySyncResultDTO> syncThirdPartyEmployees(@RequestBody EmployeeThirdPartySyncRequestDTO request);

    /**
     * 导出人员第三方同步数据。
     *
     * @param request 导出请求
     * @return 人员列表
     */
    @PostMapping("/export-third-party-employees")
    R<List<EmployeeThirdPartySyncDTO>> exportThirdPartyEmployees(@RequestBody EmployeeThirdPartySyncRequestDTO request);
}

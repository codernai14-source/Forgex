package com.forgex.common.api.feign;

import com.forgex.common.api.dto.EmployeeThirdPartyInvokeDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncResultDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 人员接口平台内部编排 Feign 客户端。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@FeignClient(name = "forgex-integration", contextId = "integrationInternalEmployeeFeignClient", path = "/integration/internal/employee")
public interface IntegrationInternalEmployeeFeignClient {

    /**
     * 发起人员出站同步。
     *
     * @param request 调用请求
     * @return 同步结果
     */
    @PostMapping("/sync")
    R<EmployeeThirdPartySyncResultDTO> syncEmployees(@RequestBody EmployeeThirdPartyInvokeDTO request);

    /**
     * 从第三方拉取人员数据并写入基础数据模块。
     *
     * @param request 调用请求
     * @return 同步结果
     */
    @PostMapping("/pull")
    R<EmployeeThirdPartySyncResultDTO> pullEmployees(@RequestBody EmployeeThirdPartyInvokeDTO request);
}

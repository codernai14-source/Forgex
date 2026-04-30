package com.forgex.common.api.feign;

import com.forgex.common.api.dto.SupplierThirdPartyInvokeDTO;
import com.forgex.common.api.dto.SupplierThirdPartySyncResultDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 供应商接口平台内部编排 Feign 客户端
 * <p>
 * 供基础数据模块调用接口平台触发供应商第三方同步。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@FeignClient(name = "forgex-integration", contextId = "integrationInternalSupplierFeignClient", path = "/api/integration/internal/supplier")
public interface IntegrationInternalSupplierFeignClient {

    /**
     * 发起供应商出站同步
     *
     * @param request 调用请求
     * @return 同步结果
     */
    @PostMapping("/sync")
    R<SupplierThirdPartySyncResultDTO> syncSuppliers(@RequestBody SupplierThirdPartyInvokeDTO request);

    /**
     * 从第三方拉取供应商数据并写入基础数据模块
     *
     * @param request 调用请求
     * @return 同步结果
     */
    @PostMapping("/pull")
    R<SupplierThirdPartySyncResultDTO> pullSuppliers(@RequestBody SupplierThirdPartyInvokeDTO request);
}

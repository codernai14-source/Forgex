package com.forgex.common.api.feign;

import com.forgex.common.api.dto.SupplierAggregateDTO;
import com.forgex.common.api.dto.SupplierThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.SupplierThirdPartySyncResultDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 供应商第三方同步 Feign 客户端
 * <p>
 * 供接口平台调用基础数据模块导出或写入供应商第三方同步数据。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@FeignClient(name = "forgex-basic", contextId = "integrationSupplierSyncFeignClient", path = "/basic/supplier/internal")
public interface IntegrationSupplierSyncFeignClient {

    /**
     * 同步第三方供应商数据到基础数据模块
     *
     * @param request 同步请求
     * @return 同步结果
     */
    @PostMapping("/sync-third-party-suppliers")
    R<SupplierThirdPartySyncResultDTO> syncThirdPartySuppliers(@RequestBody SupplierThirdPartySyncRequestDTO request);

    /**
     * 导出全部供应商第三方同步数据
     *
     * @param request 导出请求
     * @return 供应商聚合列表
     */
    @PostMapping("/export-third-party-suppliers")
    R<List<SupplierAggregateDTO>> exportThirdPartySuppliers(@RequestBody SupplierThirdPartySyncRequestDTO request);
}

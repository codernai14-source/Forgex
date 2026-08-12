package com.forgex.common.api.feign;

import com.forgex.common.api.dto.MaterialThirdPartyInvokeDTO;
import com.forgex.common.api.dto.MaterialThirdPartySyncResultDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 物料接口平台内部编排 Feign 客户端。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-12
 */
@FeignClient(name = "forgex-integration", contextId = "integrationInternalMaterialFeignClient", path = "/integration/internal/material")
public interface IntegrationInternalMaterialFeignClient {

    /**
     * 发起物料出站同步。
     *
     * @param request 调用请求
     * @return 同步结果
     */
    @PostMapping("/sync")
    R<MaterialThirdPartySyncResultDTO> syncMaterials(@RequestBody MaterialThirdPartyInvokeDTO request);

    /**
     * 从第三方拉取物料并写入基础数据模块。
     *
     * @param request 调用请求
     * @return 同步结果
     */
    @PostMapping("/pull")
    R<MaterialThirdPartySyncResultDTO> pullMaterials(@RequestBody MaterialThirdPartyInvokeDTO request);
}

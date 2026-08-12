package com.forgex.common.api.feign;

import com.forgex.common.api.dto.MaterialAggregateDTO;
import com.forgex.common.api.dto.MaterialThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.MaterialThirdPartySyncResultDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 物料第三方同步 Feign 客户端。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-12
 */
@FeignClient(name = "forgex-basic", contextId = "integrationMaterialSyncFeignClient", path = "/basic/material/internal")
public interface IntegrationMaterialSyncFeignClient {

    /**
     * 同步第三方物料到基础数据模块。
     *
     * @param request 同步请求
     * @return 同步结果
     */
    @PostMapping("/sync-third-party-materials")
    R<MaterialThirdPartySyncResultDTO> syncThirdPartyMaterials(@RequestBody MaterialThirdPartySyncRequestDTO request);

    /**
     * 导出物料第三方同步数据。
     *
     * @param request 导出请求
     * @return 物料聚合数据
     */
    @PostMapping("/export-third-party-materials")
    R<List<MaterialAggregateDTO>> exportThirdPartyMaterials(@RequestBody MaterialThirdPartySyncRequestDTO request);
}

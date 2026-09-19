package com.forgex.common.api.feign;

import com.forgex.common.api.dto.CustomerAggregateDTO;
import com.forgex.common.api.dto.CustomerThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.CustomerThirdPartySyncResultDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 客户第三方同步 Feign 客户端。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@FeignClient(name = "forgex-basic", contextId = "integrationCustomerSyncFeignClient", path = "/basic/customer/internal")
public interface IntegrationCustomerSyncFeignClient {

    @PostMapping("/sync-third-party-customers")
    R<CustomerThirdPartySyncResultDTO> syncThirdPartyCustomers(@RequestBody CustomerThirdPartySyncRequestDTO request);

    @PostMapping("/export-third-party-customers")
    R<List<CustomerAggregateDTO>> exportThirdPartyCustomers(@RequestBody CustomerThirdPartySyncRequestDTO request);
}

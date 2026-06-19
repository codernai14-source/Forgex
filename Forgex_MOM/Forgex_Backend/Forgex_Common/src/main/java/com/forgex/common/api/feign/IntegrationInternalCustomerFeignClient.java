package com.forgex.common.api.feign;

import com.forgex.common.api.dto.CustomerThirdPartyInvokeDTO;
import com.forgex.common.api.dto.CustomerThirdPartySyncResultDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 客户接口平台内部编排 Feign 客户端。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@FeignClient(name = "forgex-integration", contextId = "integrationInternalCustomerFeignClient", path = "/integration/internal/customer")
public interface IntegrationInternalCustomerFeignClient {

    @PostMapping("/sync")
    R<CustomerThirdPartySyncResultDTO> syncCustomers(@RequestBody CustomerThirdPartyInvokeDTO request);

    @PostMapping("/pull")
    R<CustomerThirdPartySyncResultDTO> pullCustomers(@RequestBody CustomerThirdPartyInvokeDTO request);
}

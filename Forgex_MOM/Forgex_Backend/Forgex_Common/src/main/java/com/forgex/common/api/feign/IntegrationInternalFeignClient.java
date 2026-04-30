package com.forgex.common.api.feign;

import com.forgex.common.api.dto.UserThirdPartyInvokeDTO;
import com.forgex.common.api.dto.UserThirdPartyPullResultDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "forgex-integration", contextId = "integrationInternalFeignClient", path = "/api/integration/internal/user")
public interface IntegrationInternalFeignClient {

    @PostMapping("/sync")
    R<UserThirdPartyPullResultDTO> syncUsers(@RequestBody UserThirdPartyInvokeDTO request);

    @PostMapping("/pull")
    R<UserThirdPartyPullResultDTO> pullUsers(@RequestBody UserThirdPartyInvokeDTO request);
}

package com.forgex.common.api.feign;

import com.forgex.common.api.dto.UserThirdPartyPullResultDTO;
import com.forgex.common.api.dto.UserThirdPartySyncDTO;
import com.forgex.common.api.dto.UserThirdPartySyncRequestDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "forgex-sys", contextId = "integrationUserSyncFeignClient", path = "/sys/user/internal")
public interface IntegrationUserSyncFeignClient {

    @PostMapping("/sync-third-party-users")
    R<UserThirdPartyPullResultDTO> syncThirdPartyUsers(@RequestBody UserThirdPartySyncRequestDTO request);

    @PostMapping("/export-third-party-users")
    R<List<UserThirdPartySyncDTO>> exportThirdPartyUsers(@RequestBody UserThirdPartySyncRequestDTO request);
}

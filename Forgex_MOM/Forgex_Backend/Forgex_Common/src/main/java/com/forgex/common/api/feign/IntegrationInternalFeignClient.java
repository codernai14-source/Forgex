package com.forgex.common.api.feign;

import com.forgex.common.api.dto.UserThirdPartyInvokeDTO;
import com.forgex.common.api.dto.UserThirdPartyPullResultDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 集成平台内部 Feign 客户端接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@FeignClient(name = "forgex-integration", contextId = "integrationInternalFeignClient", path = "/api/integration/internal/user")
public interface IntegrationInternalFeignClient {

    /**
     * 执行integrationinternalfeign客户端的同步users操作。
     *
     * @param request 请求参数
     * @return 统一响应结果
     */
    @PostMapping("/sync")
    R<UserThirdPartyPullResultDTO> syncUsers(@RequestBody UserThirdPartyInvokeDTO request);

    /**
     * 执行integrationinternalfeign客户端的pullusers操作。
     *
     * @param request 请求参数
     * @return 统一响应结果
     */
    @PostMapping("/pull")
    R<UserThirdPartyPullResultDTO> pullUsers(@RequestBody UserThirdPartyInvokeDTO request);
}

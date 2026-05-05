package com.forgex.common.api.feign;

import com.forgex.common.api.dto.UserThirdPartyPullResultDTO;
import com.forgex.common.api.dto.UserThirdPartySyncDTO;
import com.forgex.common.api.dto.UserThirdPartySyncRequestDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 集成平台用户同步 Feign 客户端接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@FeignClient(name = "forgex-sys", contextId = "integrationUserSyncFeignClient", path = "/sys/user/internal")
public interface IntegrationUserSyncFeignClient {

    /**
     * 执行integration用户同步feign客户端的同步thirdpartyusers操作。
     *
     * @param request 请求参数
     * @return 统一响应结果
     */
    @PostMapping("/sync-third-party-users")
    R<UserThirdPartyPullResultDTO> syncThirdPartyUsers(@RequestBody UserThirdPartySyncRequestDTO request);

    /**
     * 执行integration用户同步feign客户端的exportthirdpartyusers操作。
     *
     * @param request 请求参数
     * @return 统一响应结果
     */
    @PostMapping("/export-third-party-users")
    R<List<UserThirdPartySyncDTO>> exportThirdPartyUsers(@RequestBody UserThirdPartySyncRequestDTO request);
}

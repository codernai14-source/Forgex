package com.company.order.platform;

import com.forgex.common.api.dto.UserInfoDTO;
import com.forgex.common.api.feign.SysUserFeignClient;
import com.forgex.common.web.R;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 订单服务按需查询平台用户，HTTP 目标由配置指定。 */
@RestController
@Profile("platform")
public class PlatformUserController {
    private final SysUserFeignClient userClient;

    /**
     * 注入明确启用的用户客户端。
     * @param userClient 平台用户契约
     */
    public PlatformUserController(SysUserFeignClient userClient) {
        this.userClient = userClient;
    }

    /**
     * 调用平台用户查询接口。
     * @param request 用户查询参数
     * @return 平台返回的用户信息
     */
    @PostMapping("/orders/platform-user")
    public R<UserInfoDTO> user(@RequestBody UserRequest request) {
        return userClient.getUserById(request.userId());
    }

    /**
     * 用户查询参数。
     * @param userId 用户编号
     */
    public record UserRequest(Long userId) {
    }
}

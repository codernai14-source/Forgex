/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.common.api.feign;

import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 认证服务 Feign 客户端
 * <p>通过 Feign 调用 Auth 模块的认证服务接口</p>
 * 
 * @author coder_nai@163.com
 * @date 2026-01-27
 */
@FeignClient(name = "forgex-auth", contextId = "authFeignClient", path = "/auth")
public interface AuthFeignClient {
    
    /**
     * 验证 Token 是否有效
     * 
     * @param token 令牌
     * @return 是否有效
     */
    @GetMapping("/validate/{token}")
    R<Boolean> validateToken(@PathVariable("token") String token);
    
    /**
     * 根据 Token 获取用户ID
     * 
     * @param token 令牌
     * @return 用户ID
     */
    @GetMapping("/user-id/{token}")
    R<Long> getUserIdByToken(@PathVariable("token") String token);
}




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
package com.forgex.sys.client;

import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

/**
 * 认证服务权限 Feign 客户端。
 * <p>
 * 用于系统管理模块调用 Auth 模块的权限服务，获取用户的按钮权限键集合。
 * Auth 模块作为权限中心，统一提供所有业务模块的权限查询服务。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@FeignClient(name = "forgex-auth", contextId = "authPermClient")
public interface AuthPermClient {

    /**
     * 获取用户在指定租户下拥有的按钮权限键集合。
     * <p>
     * 通过 Feign 调用 Auth 模块内部接口，获取权限键列表，
     * 用于系统管理模块的接口权限鉴权。
     * </p>
     *
     * @param userId   用户 ID
     * @param tenantId 租户 ID
     * @return 权限键集合（去重）
     */
    @GetMapping("/auth/perm/internal/keys/{userId}/{tenantId}")
    R<Set<String>> getPermKeys(@PathVariable("userId") Long userId, @PathVariable("tenantId") Long tenantId);
}
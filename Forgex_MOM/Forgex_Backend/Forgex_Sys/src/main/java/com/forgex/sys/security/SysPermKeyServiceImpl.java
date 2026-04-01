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
package com.forgex.sys.security;

import com.forgex.common.security.perm.PermKeyService;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.client.AuthPermClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

/**
 * Sys 模块 PermKeyService 实现类。
 * <p>
 * 通过 Feign 调用 Auth 模块的权限服务，获取用户的按钮权限键集合。
 * Auth 模块作为权限中心，统一提供所有业务模块的权限查询服务。
 * </p>
 *
 * <p>
 * 计算口径（由 Auth 模块实现）：
 * {@code sys_user_role_perm -> sys_role_menu_perm -> sys_menu（catalog/menu/button 等 perm_key）}。
 * </p>
 *
 * <p>
 * 注意：使用 {@link Lazy} 延迟加载 {@link AuthPermClient}，避免循环依赖。
 * 循环依赖链：WebMvcConfig → PermissionInterceptor → PermKeyService → AuthPermClient → WebMvcAutoConfiguration
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see PermKeyService
 * @see AuthPermClient
 */
@Slf4j
@Service
public class SysPermKeyServiceImpl implements PermKeyService {

    private final AuthPermClient authPermClient;

    /**
     * 构造函数，使用 {@link Lazy} 延迟加载避免循环依赖。
     *
     * @param authPermClient 认证服务权限 Feign 客户端（延迟加载）
     */
    public SysPermKeyServiceImpl(@Lazy AuthPermClient authPermClient) {
        this.authPermClient = authPermClient;
    }

    /**
     * 获取用户在指定租户下拥有的按钮权限键集合。
     * <p>
     * 通过 Feign 调用 Auth 模块内部接口，获取权限键列表。
     * 若调用失败或返回空值，返回空集合。
     * </p>
     *
     * @param userId   用户 ID
     * @param tenantId 租户 ID
     * @return 权限键集合（去重），调用失败返回空集合
     */
    @Override
    public Set<String> getPermKeys(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return Collections.emptySet();
        }

        try {
            R<Set<String>> result = authPermClient.getPermKeys(userId, tenantId);
            if (result != null && result.getCode() != null && result.getCode() == StatusCode.SUCCESS && result.getData() != null) {
                return result.getData();
            }
            log.warn("获取权限键失败：userId={}, tenantId={}, msg={}", userId, tenantId,
                    result != null ? result.getMessage() : "null response");
            return Collections.emptySet();
        } catch (Exception e) {
            log.error("调用 Auth 权限服务异常：userId={}, tenantId={}", userId, tenantId, e);
            return Collections.emptySet();
        }
    }
}
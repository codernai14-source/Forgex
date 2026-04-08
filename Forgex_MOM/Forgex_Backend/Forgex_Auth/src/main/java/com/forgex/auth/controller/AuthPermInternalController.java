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
package com.forgex.auth.controller;

import com.forgex.common.security.perm.PermKeyService;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * 权限键内部接口 Controller。
 * <p>
 * 提供统一的权限键查询服务，供其他业务模块通过 Feign 调用。
 * 作为权限中心，Auth 模块负责所有业务模块的权限键计算。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see PermKeyService
 */
@RestController
@RequestMapping("/auth/perm/internal")
@RequiredArgsConstructor
public class AuthPermInternalController {

    /**
     * 权限键服务
     */
    private final PermKeyService permKeyService;

    /**
     * 获取用户在指定租户下拥有的按钮权限键集合。
     * <p>
     * 供其他业务模块通过 Feign 调用，用于接口权限鉴权。
     * 计算口径：sys_user_role_perm -> sys_role_menu_perm -> sys_menu（catalog/menu/button 等带 perm_key 的节点）
     * </p>
     *
     * @param userId   用户 ID
     * @param tenantId 租户 ID
     * @return {@link R} 包含权限键集合的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 权限键集合（Set&lt;String&gt;）
     *         - message: 提示信息
     * @throws com.forgex.common.exception.BusinessException 用户或租户不存在时抛出
     * @see PermKeyService#getPermKeys(Long, Long)
     */
    @GetMapping("/keys/{userId}/{tenantId}")
    public R<Set<String>> getPermKeys(@PathVariable Long userId, @PathVariable Long tenantId) {
        Set<String> permKeys = permKeyService.getPermKeys(userId, tenantId);
        return R.ok(permKeys);
    }
}
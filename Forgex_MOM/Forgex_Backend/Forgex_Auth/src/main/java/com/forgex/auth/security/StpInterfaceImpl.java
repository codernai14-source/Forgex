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
package com.forgex.auth.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.forgex.common.security.perm.PermKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * Sa-Token 权限与角色数据提供实现
 * 通过登录ID动态返回权限与角色列表
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final PermKeyService permKeyService;

    /**
     * 返回权限列表
     * @param loginId 登录ID
     * @param loginType 登录类型
     * @return 权限标识列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSessionByLoginId(loginId, false);
        if (session == null) {
            return java.util.Collections.emptyList();
        }

        Long userId = parseLong(session.get("LOGIN_USER_ID"));
        Long tenantId = parseLong(session.get("LOGIN_TENANT_ID"));
        if (userId == null || tenantId == null) {
            return java.util.Collections.emptyList();
        }

        Set<String> permKeys = permKeyService.getPermKeys(userId, tenantId);
        if (permKeys == null || permKeys.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return permKeys.stream().filter(StringUtils::hasText).sorted().toList();
    }

    /**
     * 返回角色列表
     * @param loginId 登录ID
     * @param loginType 登录类型
     * @return 角色标识列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return java.util.Collections.emptyList();
    }

    /**
     * 解析 Long 类型参数。
     *
     * @param obj 入参
     * @return Long 值（无法解析返回 null）
     */
    private Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.valueOf((String) obj);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}

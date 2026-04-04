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

import com.baomidou.dynamic.datasource.annotation.DS;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.forgex.common.security.perm.PermKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Sa-Token 权限与角色数据提供实现
 * 通过登录ID动态返回权限与角色列表
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private static final String QUERY_ROLE_KEYS_SQL = """
            SELECT DISTINCT r.role_key
            FROM sys_user_role ur
            INNER JOIN sys_role r
                ON r.id = ur.role_id
               AND r.tenant_id = ur.tenant_id
            WHERE ur.user_id = ?
              AND ur.tenant_id = ?
              AND IFNULL(r.deleted, 0) = 0
              AND r.role_key IS NOT NULL
              AND r.role_key <> ''
            ORDER BY r.role_key
            """;

    private final PermKeyService permKeyService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 返回权限列表
     * @param loginId 登录ID
     * @param loginType 登录类型
     * @return 权限标识列表
     */
    @Override
    @DS("admin")
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
    @DS("admin")
    public List<String> getRoleList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSessionByLoginId(loginId, false);
        if (session == null) {
            return Collections.emptyList();
        }

        Long userId = parseLong(session.get("LOGIN_USER_ID"));
        Long tenantId = parseLong(session.get("LOGIN_TENANT_ID"));
        if (userId == null || tenantId == null) {
            return Collections.emptyList();
        }

        List<String> roleKeys = jdbcTemplate.queryForList(
                QUERY_ROLE_KEYS_SQL,
                String.class,
                userId,
                tenantId
        );
        if (roleKeys == null || roleKeys.isEmpty()) {
            return Collections.emptyList();
        }

        return roleKeys.stream()
                .filter(StringUtils::hasText)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
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

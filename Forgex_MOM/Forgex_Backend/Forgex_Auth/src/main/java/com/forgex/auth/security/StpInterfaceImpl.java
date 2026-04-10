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
import com.baomidou.dynamic.datasource.annotation.DS;
import com.forgex.common.security.perm.PermKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限与角色数据提供实现。
 * <p>
 * 实现 StpInterface 接口，为 Sa-Token 框架提供用户的权限标识和角色标识列表。
 * 通过登录 ID 从 Session 中获取用户 ID 和租户 ID，然后查询数据库获取对应的权限和角色。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-08
 * @see StpInterface
 * @see PermKeyService
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    /**
     * 查询用户角色键的 SQL 语句。
     * <p>
     * 通过用户 ID 和租户 ID 关联用户角色表和角色表，获取用户拥有的所有角色键。
     * </p>
     */
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

    /**
     * 权限键服务，用于获取用户的权限标识列表。
     */
    private final PermKeyService permKeyService;

    /**
     * Spring JdbcTemplate，用于执行原生 SQL 查询角色列表。
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 返回用户的权限标识列表。
     * <p>
     * 从 Session 中获取登录用户 ID 和租户 ID，然后通过 PermKeyService 查询用户的权限键集合，
     * 转换为排序后的列表返回。
     * </p>
     *
     * @param loginId   登录 ID，通常是用户 ID 的字符串形式
     * @param loginType 登录类型，由 Sa-Token 框架传入
     * @return 权限标识列表（已排序），如果 Session 不存在或用户/租户 ID 为空则返回空列表
     * @see StpInterface#getPermissionList(Object, String)
     */
    @Override
    @DS("admin")
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 获取 Session 对象（不创建新 Session）
        SaSession session = StpUtil.getSessionByLoginId(loginId, false);
        if (session == null) {
            return Collections.emptyList();
        }

        // 从 Session 中获取用户 ID 和租户 ID
        Long userId = parseLong(session.get("LOGIN_USER_ID"));
        Long tenantId = parseLong(session.get("LOGIN_TENANT_ID"));
        if (userId == null || tenantId == null) {
            return Collections.emptyList();
        }

        // 通过 PermKeyService 获取权限键集合
        Set<String> permKeys = permKeyService.getPermKeys(userId, tenantId);
        if (permKeys == null || permKeys.isEmpty()) {
            return Collections.emptyList();
        }

        // 过滤空值并排序后返回
        return permKeys.stream()
                .filter(StringUtils::hasText)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 返回用户的角色标识列表。
     * <p>
     * 从 Session 中获取登录用户 ID 和租户 ID，然后通过 SQL 查询用户的角色键列表。
     * </p>
     *
     * @param loginId   登录 ID，通常是用户 ID 的字符串形式
     * @param loginType 登录类型，由 Sa-Token 框架传入
     * @return 角色标识列表（去重），如果 Session 不存在或用户/租户 ID 为空则返回空列表
     * @see StpInterface#getRoleList(Object, String)
     */
    @Override
    @DS("admin")
    public List<String> getRoleList(Object loginId, String loginType) {
        // 获取 Session 对象（不创建新 Session）
        SaSession session = StpUtil.getSessionByLoginId(loginId, false);
        if (session == null) {
            return Collections.emptyList();
        }

        // 从 Session 中获取用户 ID 和租户 ID
        Long userId = parseLong(session.get("LOGIN_USER_ID"));
        Long tenantId = parseLong(session.get("LOGIN_TENANT_ID"));
        if (userId == null || tenantId == null) {
            return Collections.emptyList();
        }

        // 执行 SQL 查询获取角色键列表
        List<String> roleKeys = jdbcTemplate.queryForList(
                QUERY_ROLE_KEYS_SQL,
                String.class,
                userId,
                tenantId
        );
        if (roleKeys == null || roleKeys.isEmpty()) {
            return Collections.emptyList();
        }

        // 过滤空值并去重后返回
        return roleKeys.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .collect(Collectors.toList());
    }

    /**
     * 解析 Long 类型参数。
     * <p>
     * 支持 Number 和 String 类型的输入，如果无法解析则返回 null。
     * </p>
     *
     * @param obj 待解析的对象
     * @return Long 值，如果无法解析或输入为 null 则返回 null
     */
    private Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        // 如果是 Number 类型，直接转换为 long
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        // 如果是 String 类型，尝试解析
        if (obj instanceof String) {
            try {
                return Long.valueOf((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}

package com.forgex.auth.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.forgex.common.security.LoginSessionKeys;
import com.forgex.common.security.LoginSessionSupport;
import com.forgex.common.security.perm.PermKeyService;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Sa-Token 权限与角色提供实现。
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

    @Override
    @DS("admin")
    public List<String> getPermissionList(Object loginId, String loginType) {
        SaSession session = resolveSession(loginId);
        if (session == null) {
            return Collections.emptyList();
        }

        Long userId = parseLong(session.get(LoginSessionKeys.KEY_USER_ID));
        Long tenantId = parseLong(session.get(LoginSessionKeys.KEY_TENANT_ID));
        if (userId == null || tenantId == null) {
            return Collections.emptyList();
        }

        Set<String> permKeys = permKeyService.getPermKeys(userId, tenantId);
        if (permKeys == null || permKeys.isEmpty()) {
            return Collections.emptyList();
        }

        return permKeys.stream()
                .filter(StringUtils::hasText)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    @DS("admin")
    public List<String> getRoleList(Object loginId, String loginType) {
        SaSession session = resolveSession(loginId);
        if (session == null) {
            return Collections.emptyList();
        }

        Long userId = parseLong(session.get(LoginSessionKeys.KEY_USER_ID));
        Long tenantId = parseLong(session.get(LoginSessionKeys.KEY_TENANT_ID));
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
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .collect(Collectors.toList());
    }

    private SaSession resolveSession(Object loginId) {
        String token = null;
        try {
            token = StpUtil.getTokenValue();
        } catch (Exception ignored) {
        }
        return LoginSessionSupport.getSessionByLoginId(loginId, token);
    }

    private Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number number) {
            return number.longValue();
        }
        if (obj instanceof String text) {
            try {
                return Long.valueOf(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}

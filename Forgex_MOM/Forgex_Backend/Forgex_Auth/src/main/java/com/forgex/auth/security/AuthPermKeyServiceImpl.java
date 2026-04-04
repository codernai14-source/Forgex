package com.forgex.auth.security;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.forgex.common.security.perm.PermKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Auth 模块 permKey 计算服务实现。
 * <p>
 * 计算口径与 Sys/其它业务模块保持一致：
 * {@code sys_user_role -> sys_role_menu -> sys_menu.perm_key}。
 * </p>
 * <p>
 * 这里直接使用 SQL 查询现网真实表，避免历史 Mapper 命名残留、MyBatis 插件干扰或旧表名误用导致的空权限结果。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see PermKeyService
 */
@Service
@RequiredArgsConstructor
public class AuthPermKeyServiceImpl implements PermKeyService {

    private static final String QUERY_PERM_KEYS_SQL = """
            SELECT DISTINCT m.perm_key
            FROM sys_user_role ur
            INNER JOIN sys_role_menu rm
                ON rm.role_id = ur.role_id
               AND rm.tenant_id = ur.tenant_id
            INNER JOIN sys_menu m
                ON m.id = rm.menu_id
               AND m.tenant_id = rm.tenant_id
            WHERE ur.user_id = ?
              AND ur.tenant_id = ?
              AND IFNULL(m.deleted, 0) = 0
              AND m.perm_key IS NOT NULL
              AND m.perm_key <> ''
              AND LOWER(m.type) IN ('button', 'menu', 'catalog')
            ORDER BY m.perm_key
            """;

    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取用户在指定租户下拥有的按钮权限键集合。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 权限键集合（去重）
     */
    @Override
    @DS("admin")
    public Set<String> getPermKeys(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return Collections.emptySet();
        }

        List<String> permKeys = jdbcTemplate.queryForList(
                QUERY_PERM_KEYS_SQL,
                String.class,
                userId,
                tenantId
        );
        if (permKeys == null || permKeys.isEmpty()) {
            return Collections.emptySet();
        }

        return permKeys.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}


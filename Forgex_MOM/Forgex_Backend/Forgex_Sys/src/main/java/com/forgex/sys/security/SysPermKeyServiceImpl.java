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

import com.baomidou.dynamic.datasource.annotation.DS;
import com.forgex.common.security.perm.PermKeyService;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.client.AuthPermClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sys module permission key service.
 * Local DB lookup is preferred to avoid remote dependency failures.
 */
@Slf4j
@Service
public class SysPermKeyServiceImpl implements PermKeyService {

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
    private final AuthPermClient authPermClient;

    public SysPermKeyServiceImpl(JdbcTemplate jdbcTemplate, @Lazy AuthPermClient authPermClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.authPermClient = authPermClient;
    }

    @Override
    @DS("admin")
    public Set<String> getPermKeys(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return Collections.emptySet();
        }

        try {
            List<String> localPermKeys = jdbcTemplate.queryForList(
                    QUERY_PERM_KEYS_SQL,
                    String.class,
                    userId,
                    tenantId
            );
            Set<String> normalized = normalize(localPermKeys);
            if (!normalized.isEmpty()) {
                return normalized;
            }
            log.warn("Local permission keys are empty, fallback to Auth service. userId={}, tenantId={}", userId, tenantId);
        } catch (Exception e) {
            log.error("Local permission key query failed, fallback to Auth service. userId={}, tenantId={}", userId, tenantId, e);
        }

        return queryPermKeysFromAuth(userId, tenantId);
    }

    private Set<String> queryPermKeysFromAuth(Long userId, Long tenantId) {
        try {
            R<Set<String>> result = authPermClient.getPermKeys(userId, tenantId);
            if (result != null
                    && result.getCode() != null
                    && result.getCode() == StatusCode.SUCCESS
                    && result.getData() != null) {
                Set<String> normalized = normalize(result.getData());
                if (!normalized.isEmpty()) {
                    return normalized;
                }
            }

            log.warn("Auth permission key query returned empty. userId={}, tenantId={}, msg={}",
                    userId,
                    tenantId,
                    result != null ? result.getMessage() : "null response");
        } catch (Exception e) {
            log.error("Auth permission key query failed. userId={}, tenantId={}", userId, tenantId, e);
        }

        return Collections.emptySet();
    }

    private Set<String> normalize(List<String> permKeys) {
        if (permKeys == null || permKeys.isEmpty()) {
            return Collections.emptySet();
        }
        return permKeys.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> normalize(Set<String> permKeys) {
        if (permKeys == null || permKeys.isEmpty()) {
            return Collections.emptySet();
        }
        return permKeys.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

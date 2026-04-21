package com.forgex.integration.security;

import com.forgex.common.api.feign.AuthPermClient;
import com.forgex.common.security.perm.PermKeyService;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Integration 模块权限键服务实现。
 * <p>
 * 通过 Auth 模块统一获取当前用户权限，支撑集成平台后台管理接口的权限校验。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-17
 */
@Slf4j
@Service
public class IntegrationPermKeyServiceImpl implements PermKeyService {

    private final AuthPermClient authPermClient;

    public IntegrationPermKeyServiceImpl(@Lazy AuthPermClient authPermClient) {
        this.authPermClient = authPermClient;
    }

    /**
     * 查询用户在指定租户下的权限键集合。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @return 归一化后的权限键集合
     */
    @Override
    public Set<String> getPermKeys(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return Collections.emptySet();
        }

        try {
            R<Set<String>> result = authPermClient.getPermKeys(userId, tenantId);
            if (result != null
                    && result.getCode() != null
                    && result.getCode() == StatusCode.SUCCESS
                    && result.getData() != null) {
                return result.getData().stream()
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
            log.warn("Integration permission query returned empty. userId={}, tenantId={}, msg={}",
                    userId,
                    tenantId,
                    result != null ? result.getMessage() : "null response");
        } catch (Exception e) {
            log.error("Integration permission query failed. userId={}, tenantId={}", userId, tenantId, e);
        }

        return Collections.emptySet();
    }
}

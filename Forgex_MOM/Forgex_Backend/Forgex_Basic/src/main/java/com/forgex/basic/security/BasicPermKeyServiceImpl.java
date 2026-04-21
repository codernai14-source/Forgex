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
package com.forgex.basic.security;

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
 * Basic 模块权限键服务实现。
 * <p>
 * 通过 Auth 模块统一查询当前用户在当前租户下持有的权限键，
 * 让 Basic 模块的 {@code @RequirePerm} 能够真正生效。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-17
 */
@Slf4j
@Service
public class BasicPermKeyServiceImpl implements PermKeyService {

    private final AuthPermClient authPermClient;

    public BasicPermKeyServiceImpl(@Lazy AuthPermClient authPermClient) {
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
            log.warn("Basic permission query returned empty. userId={}, tenantId={}, msg={}",
                    userId,
                    tenantId,
                    result != null ? result.getMessage() : "null response");
        } catch (Exception e) {
            log.error("Basic permission query failed. userId={}, tenantId={}", userId, tenantId, e);
        }

        return Collections.emptySet();
    }
}

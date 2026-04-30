package com.forgex.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.api.dto.UserThirdPartySyncDTO;
import com.forgex.common.api.dto.UserThirdPartySyncRequestDTO;
import com.forgex.common.api.feign.IntegrationUserSyncFeignClient;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.spi.ApiInboundInterpreter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component("userThirdPartyInboundInterpreter")
@RequiredArgsConstructor
public class UserThirdPartyInboundInterpreter implements ApiInboundInterpreter {

    private final IntegrationUserSyncFeignClient integrationUserSyncFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public Object handle(ApiExecutionContext context, Object payload) {
        UserThirdPartySyncRequestDTO request = new UserThirdPartySyncRequestDTO();
        request.setTenantId(resolveTenantId(context, payload));
        request.setUsers(resolveUsers(payload));
        return integrationUserSyncFeignClient.syncThirdPartyUsers(request).getData();
    }

    @SuppressWarnings("unchecked")
    private List<UserThirdPartySyncDTO> resolveUsers(Object payload) {
        if (payload instanceof Map<?, ?> map && map.get("users") instanceof List<?>) {
            return ((List<?>) map.get("users")).stream()
                .map(this::convertUser)
                .filter(java.util.Objects::nonNull)
                .toList();
        }
        if (payload instanceof List<?>) {
            return ((List<?>) payload).stream()
                .map(this::convertUser)
                .filter(java.util.Objects::nonNull)
                .toList();
        }
        return Collections.emptyList();
    }

    private UserThirdPartySyncDTO convertUser(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof UserThirdPartySyncDTO dto) {
            return dto;
        }
        return objectMapper.convertValue(source, UserThirdPartySyncDTO.class);
    }

    private Long resolveTenantId(ApiExecutionContext context, Object payload) {
        if (payload instanceof Map<?, ?> map) {
            Object tenantId = map.get("tenantId");
            if (tenantId instanceof Number number) {
                return number.longValue();
            }
            if (tenantId instanceof String str) {
                try {
                    return Long.parseLong(str);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return context == null ? null : context.getTenantId();
    }
}

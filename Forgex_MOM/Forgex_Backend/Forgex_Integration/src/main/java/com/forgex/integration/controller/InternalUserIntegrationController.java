package com.forgex.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.api.dto.UserThirdPartyInvokeDTO;
import com.forgex.common.api.dto.UserThirdPartyPullResultDTO;
import com.forgex.common.api.dto.UserThirdPartySyncDTO;
import com.forgex.common.api.dto.UserThirdPartySyncRequestDTO;
import com.forgex.common.api.feign.IntegrationUserSyncFeignClient;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.integration.domain.model.IntegrationExecuteResult;
import com.forgex.integration.service.IntegrationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户集成内部控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class InternalUserIntegrationController {

    private final IntegrationFacade integrationFacade;
    private final IntegrationUserSyncFeignClient integrationUserSyncFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * 同步用户数据。
     *
     * @param request 请求参数
     * @return 统一响应结果
     */
    @PostMapping("/sync")
    public R<UserThirdPartyPullResultDTO> syncUsers(@RequestBody UserThirdPartyInvokeDTO request) {
        List<UserThirdPartySyncDTO> users = exportUsers(request.getTenantId());
        if (users.isEmpty()) {
            return R.ok(new UserThirdPartyPullResultDTO());
        }

        try {
            TenantContext.set(request.getTenantId());
            IntegrationExecuteResult result = integrationFacade.invoke(request.getApiCode(), buildSyncPayload(request, users));
            return R.ok(toPullResult(result, users.size()));
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 拉取用户数据。
     *
     * @param request 请求参数
     * @return 统一响应结果
     */
    @PostMapping("/pull")
    public R<UserThirdPartyPullResultDTO> pullUsers(@RequestBody UserThirdPartyInvokeDTO request) {
        try {
            TenantContext.set(request.getTenantId());
            IntegrationExecuteResult result = integrationFacade.invoke(request.getApiCode(), buildPullPayload(request));
            List<UserThirdPartySyncDTO> users = extractUsers(result == null ? null : result.getData());
            UserThirdPartySyncRequestDTO syncRequest = new UserThirdPartySyncRequestDTO();
            syncRequest.setTenantId(request.getTenantId());
            syncRequest.setUsers(users);
            return integrationUserSyncFeignClient.syncThirdPartyUsers(syncRequest);
        } finally {
            TenantContext.clear();
        }
    }

    private List<UserThirdPartySyncDTO> exportUsers(Long tenantId) {
        UserThirdPartySyncRequestDTO request = new UserThirdPartySyncRequestDTO();
        request.setTenantId(tenantId);
        request.setUsers(Collections.emptyList());
        R<List<UserThirdPartySyncDTO>> response = integrationUserSyncFeignClient.exportThirdPartyUsers(request);
        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }
        return response.getData();
    }

    private Map<String, Object> buildSyncPayload(UserThirdPartyInvokeDTO request, List<UserThirdPartySyncDTO> users) {
        Map<String, Object> payload = request.getPayload();
        if (payload == null) {
            payload = new LinkedHashMap<>();
            request.setPayload(payload);
        }
        payload.put("tenantId", request.getTenantId());
        payload.put("users", users);
        return payload;
    }

    private Map<String, Object> buildPullPayload(UserThirdPartyInvokeDTO request) {
        Map<String, Object> payload = request.getPayload();
        if (payload == null) {
            payload = new LinkedHashMap<>();
            request.setPayload(payload);
        }
        payload.put("tenantId", request.getTenantId());
        payload.putIfAbsent("includeDisabled", Boolean.TRUE);
        return payload;
    }

    @SuppressWarnings("unchecked")
    private List<UserThirdPartySyncDTO> extractUsers(Object data) {
        Object normalized = unwrapResponseData(data);
        if (normalized instanceof String text) {
            normalized = parseJsonString(text);
        }
        if (normalized instanceof List<?>) {
            return ((List<?>) normalized).stream()
                .map(this::convertUser)
                .filter(java.util.Objects::nonNull)
                .toList();
        }
        if (normalized instanceof Map<?, ?> map) {
            Object users = map.get("users");
            if (users instanceof String text) {
                users = parseJsonString(text);
            }
            if (users instanceof List<?>) {
                return ((List<?>) users).stream()
                    .map(this::convertUser)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            }
        }
        return Collections.emptyList();
    }

    private Object unwrapResponseData(Object data) {
        Object current = data;
        if (current instanceof String text) {
            current = parseJsonString(text);
        }
        if (current instanceof Map<?, ?> map && map.containsKey("data")) {
            return map.get("data");
        }
        return current;
    }

    private Object parseJsonString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(text, Object.class);
        } catch (Exception ignored) {
            return text;
        }
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

    private UserThirdPartyPullResultDTO toPullResult(IntegrationExecuteResult result, int totalCount) {
        UserThirdPartyPullResultDTO dto = new UserThirdPartyPullResultDTO();
        dto.setTotalCount(totalCount);
        if (result != null && result.isSuccess()) {
            dto.setUpdatedCount(totalCount);
        }
        return dto;
    }
}

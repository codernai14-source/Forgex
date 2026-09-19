package com.forgex.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.api.dto.EmployeeThirdPartySyncDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncRequestDTO;
import com.forgex.common.api.feign.IntegrationEmployeeSyncFeignClient;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.spi.ApiInboundInterpreter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 人员第三方入站解释器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Component("employeeThirdPartyInboundInterpreter")
@RequiredArgsConstructor
public class EmployeeThirdPartyInboundInterpreter implements ApiInboundInterpreter {

    private final IntegrationEmployeeSyncFeignClient integrationEmployeeSyncFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public Object handle(ApiExecutionContext context, Object payload) {
        EmployeeThirdPartySyncRequestDTO request = new EmployeeThirdPartySyncRequestDTO();
        request.setTenantId(resolveTenantId(context, payload));
        request.setEmployees(resolveEmployees(payload));
        return integrationEmployeeSyncFeignClient.syncThirdPartyEmployees(request).getData();
    }

    private List<EmployeeThirdPartySyncDTO> resolveEmployees(Object payload) {
        if (payload instanceof Map<?, ?> map && map.get("employees") instanceof List<?> list) {
            return list.stream().map(this::convertEmployee).filter(java.util.Objects::nonNull).toList();
        }
        if (payload instanceof List<?> list) {
            return list.stream().map(this::convertEmployee).filter(java.util.Objects::nonNull).toList();
        }
        return Collections.emptyList();
    }

    private EmployeeThirdPartySyncDTO convertEmployee(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof EmployeeThirdPartySyncDTO dto) {
            return dto;
        }
        return objectMapper.convertValue(source, EmployeeThirdPartySyncDTO.class);
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

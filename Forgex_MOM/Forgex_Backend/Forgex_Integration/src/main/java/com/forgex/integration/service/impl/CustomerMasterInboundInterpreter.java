package com.forgex.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.api.dto.CustomerAggregateDTO;
import com.forgex.common.api.dto.CustomerThirdPartySyncRequestDTO;
import com.forgex.common.api.feign.IntegrationCustomerSyncFeignClient;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.spi.ApiInboundInterpreter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 客户主数据入站解释器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Component("customerMasterInboundInterpreter")
@RequiredArgsConstructor
public class CustomerMasterInboundInterpreter implements ApiInboundInterpreter {

    private final IntegrationCustomerSyncFeignClient integrationCustomerSyncFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * 处理导入数据。
     *
     * @param context 接口平台执行上下文
     * @param payload 已按接口配置装配后的报文
     * @return 基础数据写入结果
     */
    @Override
    public Object handle(ApiExecutionContext context, Object payload) {
        CustomerThirdPartySyncRequestDTO request = new CustomerThirdPartySyncRequestDTO();
        request.setTenantId(resolveTenantId(context, payload));
        request.setCustomers(resolveCustomers(payload));
        return integrationCustomerSyncFeignClient.syncThirdPartyCustomers(request).getData();
    }

    @SuppressWarnings("unchecked")
    private List<CustomerAggregateDTO> resolveCustomers(Object payload) {
        if (payload instanceof Map<?, ?> map && map.get("customers") instanceof List<?>) {
            return ((List<?>) map.get("customers")).stream()
                    .map(this::convertCustomer)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        if (payload instanceof List<?>) {
            return ((List<?>) payload).stream()
                    .map(this::convertCustomer)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        return Collections.emptyList();
    }

    private CustomerAggregateDTO convertCustomer(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof CustomerAggregateDTO dto) {
            return dto;
        }
        return objectMapper.convertValue(source, CustomerAggregateDTO.class);
    }

    private Long resolveTenantId(ApiExecutionContext context, Object payload) {
        if (payload instanceof Map<?, ?> map) {
            Object tenantId = map.get("tenantId");
            if (tenantId instanceof Number number) {
                return number.longValue();
            }
            if (tenantId instanceof String str && !str.isBlank()) {
                try {
                    return Long.parseLong(str);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return context == null ? null : context.getTenantId();
    }
}

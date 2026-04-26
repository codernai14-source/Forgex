package com.forgex.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.api.dto.SupplierAggregateDTO;
import com.forgex.common.api.dto.SupplierThirdPartySyncRequestDTO;
import com.forgex.common.api.feign.IntegrationSupplierSyncFeignClient;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.spi.ApiInboundInterpreter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 供应商主数据入站解释器。
 * <p>
 * 接收接口平台 public inbound 报文，抽取供应商聚合列表后写入基础数据模块。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
@Component("supplierMasterInboundInterpreter")
@RequiredArgsConstructor
public class SupplierMasterInboundInterpreter implements ApiInboundInterpreter {

    private final IntegrationSupplierSyncFeignClient integrationSupplierSyncFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * 处理供应商主数据入站报文。
     *
     * @param context 接口平台执行上下文
     * @param payload 已按接口配置装配后的报文
     * @return 基础数据写入结果
     */
    @Override
    public Object handle(ApiExecutionContext context, Object payload) {
        SupplierThirdPartySyncRequestDTO request = new SupplierThirdPartySyncRequestDTO();
        request.setTenantId(resolveTenantId(context, payload));
        request.setSuppliers(resolveSuppliers(payload));
        return integrationSupplierSyncFeignClient.syncThirdPartySuppliers(request).getData();
    }

    @SuppressWarnings("unchecked")
    private List<SupplierAggregateDTO> resolveSuppliers(Object payload) {
        if (payload instanceof Map<?, ?> map && map.get("suppliers") instanceof List<?>) {
            return ((List<?>) map.get("suppliers")).stream()
                    .map(this::convertSupplier)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        if (payload instanceof List<?>) {
            return ((List<?>) payload).stream()
                    .map(this::convertSupplier)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        return Collections.emptyList();
    }

    private SupplierAggregateDTO convertSupplier(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof SupplierAggregateDTO dto) {
            return dto;
        }
        return objectMapper.convertValue(source, SupplierAggregateDTO.class);
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

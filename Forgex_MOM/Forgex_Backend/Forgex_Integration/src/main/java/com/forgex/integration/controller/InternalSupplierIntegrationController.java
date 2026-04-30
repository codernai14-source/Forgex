package com.forgex.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.api.dto.SupplierAggregateDTO;
import com.forgex.common.api.dto.SupplierThirdPartyInvokeDTO;
import com.forgex.common.api.dto.SupplierThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.SupplierThirdPartySyncResultDTO;
import com.forgex.common.api.feign.IntegrationSupplierSyncFeignClient;
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
 * 供应商同步内部编排控制器。
 * <p>
 * 面向基础数据模块提供供应商主数据出站同步和第三方拉取写入能力，统一走接口平台配置执行。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
@RestController
@RequestMapping("/api/integration/internal/supplier")
@RequiredArgsConstructor
public class InternalSupplierIntegrationController {

    private final IntegrationFacade integrationFacade;
    private final IntegrationSupplierSyncFeignClient integrationSupplierSyncFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * 出站同步供应商全量主数据。
     *
     * @param request 接口平台调用参数
     * @return 同步结果
     */
    @PostMapping("/sync")
    public R<SupplierThirdPartySyncResultDTO> syncSuppliers(@RequestBody SupplierThirdPartyInvokeDTO request) {
        SupplierThirdPartyInvokeDTO safeRequest = request == null ? new SupplierThirdPartyInvokeDTO() : request;
        List<SupplierAggregateDTO> suppliers = exportSuppliers(safeRequest.getTenantId());
        if (suppliers.isEmpty()) {
            SupplierThirdPartySyncResultDTO empty = new SupplierThirdPartySyncResultDTO();
            empty.setTotalCount(0);
            return R.ok(empty);
        }

        try {
            TenantContext.set(safeRequest.getTenantId());
            IntegrationExecuteResult result = integrationFacade.invoke(safeRequest.getApiCode(), buildSyncPayload(safeRequest, suppliers));
            return R.ok(toSyncResult(result, suppliers.size()));
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 从第三方拉取供应商主数据并写入基础数据模块。
     *
     * @param request 接口平台调用参数
     * @return 写入结果
     */
    @PostMapping("/pull")
    public R<SupplierThirdPartySyncResultDTO> pullSuppliers(@RequestBody SupplierThirdPartyInvokeDTO request) {
        SupplierThirdPartyInvokeDTO safeRequest = request == null ? new SupplierThirdPartyInvokeDTO() : request;
        try {
            TenantContext.set(safeRequest.getTenantId());
            IntegrationExecuteResult result = integrationFacade.invoke(safeRequest.getApiCode(), safeRequest.getPayload());
            SupplierThirdPartySyncRequestDTO syncRequest = new SupplierThirdPartySyncRequestDTO();
            syncRequest.setTenantId(safeRequest.getTenantId());
            syncRequest.setSuppliers(extractSuppliers(result == null ? null : result.getData()));
            return integrationSupplierSyncFeignClient.syncThirdPartySuppliers(syncRequest);
        } finally {
            TenantContext.clear();
        }
    }

    private List<SupplierAggregateDTO> exportSuppliers(Long tenantId) {
        SupplierThirdPartySyncRequestDTO request = new SupplierThirdPartySyncRequestDTO();
        request.setTenantId(tenantId);
        request.setSuppliers(Collections.emptyList());
        R<List<SupplierAggregateDTO>> response = integrationSupplierSyncFeignClient.exportThirdPartySuppliers(request);
        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }
        return response.getData();
    }

    private Map<String, Object> buildSyncPayload(SupplierThirdPartyInvokeDTO request, List<SupplierAggregateDTO> suppliers) {
        Map<String, Object> payload = request.getPayload();
        if (payload == null) {
            payload = new LinkedHashMap<>();
            request.setPayload(payload);
        }
        payload.put("tenantId", request.getTenantId());
        payload.put("suppliers", suppliers);
        return payload;
    }

    @SuppressWarnings("unchecked")
    private List<SupplierAggregateDTO> extractSuppliers(Object data) {
        if (data instanceof List<?>) {
            return ((List<?>) data).stream()
                    .map(this::convertSupplier)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        if (data instanceof Map<?, ?> map && map.get("suppliers") instanceof List<?>) {
            return ((List<?>) map.get("suppliers")).stream()
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

    private SupplierThirdPartySyncResultDTO toSyncResult(IntegrationExecuteResult result, int totalCount) {
        SupplierThirdPartySyncResultDTO dto = new SupplierThirdPartySyncResultDTO();
        dto.setTotalCount(totalCount);
        if (result != null && result.isSuccess()) {
            dto.setUpdatedCount(totalCount);
        }
        return dto;
    }
}

package com.forgex.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.api.dto.MaterialAggregateDTO;
import com.forgex.common.api.dto.MaterialThirdPartyInvokeDTO;
import com.forgex.common.api.dto.MaterialThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.MaterialThirdPartySyncResultDTO;
import com.forgex.common.api.feign.IntegrationMaterialSyncFeignClient;
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
 * 物料同步内部编排控制器。
 * <p>
 * 面向基础数据模块提供物料主数据出站同步和第三方拉取写入能力。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-12
 */
@RestController
@RequestMapping("/internal/material")
@RequiredArgsConstructor
public class InternalMaterialIntegrationController {

    private final IntegrationFacade integrationFacade;
    private final IntegrationMaterialSyncFeignClient integrationMaterialSyncFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * 出站同步物料主数据。
     *
     * @param request 接口平台调用参数
     * @return 同步结果
     */
    @PostMapping("/sync")
    public R<MaterialThirdPartySyncResultDTO> syncMaterials(@RequestBody MaterialThirdPartyInvokeDTO request) {
        MaterialThirdPartyInvokeDTO safeRequest = request == null ? new MaterialThirdPartyInvokeDTO() : request;
        List<MaterialAggregateDTO> materials = exportMaterials(safeRequest.getTenantId());
        if (materials.isEmpty()) {
            MaterialThirdPartySyncResultDTO empty = new MaterialThirdPartySyncResultDTO();
            empty.setTotalCount(0);
            return R.ok(empty);
        }

        try {
            TenantContext.set(safeRequest.getTenantId());
            IntegrationExecuteResult result = integrationFacade.invoke(safeRequest.getApiCode(), buildSyncPayload(safeRequest, materials));
            return R.ok(toSyncResult(result, materials.size()));
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 从第三方拉取物料主数据并写入基础数据模块。
     *
     * @param request 接口平台调用参数
     * @return 写入结果
     */
    @PostMapping("/pull")
    public R<MaterialThirdPartySyncResultDTO> pullMaterials(@RequestBody MaterialThirdPartyInvokeDTO request) {
        MaterialThirdPartyInvokeDTO safeRequest = request == null ? new MaterialThirdPartyInvokeDTO() : request;
        try {
            TenantContext.set(safeRequest.getTenantId());
            IntegrationExecuteResult result = integrationFacade.invoke(safeRequest.getApiCode(), buildPullPayload(safeRequest));
            MaterialThirdPartySyncRequestDTO syncRequest = new MaterialThirdPartySyncRequestDTO();
            syncRequest.setTenantId(safeRequest.getTenantId());
            syncRequest.setMaterials(extractMaterials(result == null ? null : result.getData()));
            return integrationMaterialSyncFeignClient.syncThirdPartyMaterials(syncRequest);
        } finally {
            TenantContext.clear();
        }
    }

    private List<MaterialAggregateDTO> exportMaterials(Long tenantId) {
        MaterialThirdPartySyncRequestDTO request = new MaterialThirdPartySyncRequestDTO();
        request.setTenantId(tenantId);
        request.setMaterials(Collections.emptyList());
        R<List<MaterialAggregateDTO>> response = integrationMaterialSyncFeignClient.exportThirdPartyMaterials(request);
        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }
        return response.getData();
    }

    private Map<String, Object> buildSyncPayload(MaterialThirdPartyInvokeDTO request, List<MaterialAggregateDTO> materials) {
        Map<String, Object> payload = request.getPayload();
        if (payload == null) {
            payload = new LinkedHashMap<>();
            request.setPayload(payload);
        }
        payload.put("tenantId", request.getTenantId());
        payload.put("materials", materials);
        return payload;
    }

    private Map<String, Object> buildPullPayload(MaterialThirdPartyInvokeDTO request) {
        Map<String, Object> payload = request.getPayload();
        if (payload == null) {
            payload = new LinkedHashMap<>();
            request.setPayload(payload);
        }
        payload.put("tenantId", request.getTenantId());
        return payload;
    }

    private List<MaterialAggregateDTO> extractMaterials(Object data) {
        Object normalized = unwrapResponseData(data);
        if (normalized instanceof String text) {
            normalized = parseJsonString(text);
        }
        if (normalized instanceof List<?>) {
            return ((List<?>) normalized).stream()
                    .map(this::convertMaterial)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        if (normalized instanceof Map<?, ?> map) {
            Object materials = map.get("materials");
            if (materials instanceof String text) {
                materials = parseJsonString(text);
            }
            if (materials instanceof List<?>) {
                return ((List<?>) materials).stream()
                        .map(this::convertMaterial)
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

    private MaterialAggregateDTO convertMaterial(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof MaterialAggregateDTO dto) {
            return dto;
        }
        return objectMapper.convertValue(source, MaterialAggregateDTO.class);
    }

    private MaterialThirdPartySyncResultDTO toSyncResult(IntegrationExecuteResult result, int totalCount) {
        MaterialThirdPartySyncResultDTO dto = new MaterialThirdPartySyncResultDTO();
        dto.setTotalCount(totalCount);
        if (result != null && result.isSuccess()) {
            dto.setUpdatedCount(totalCount);
        }
        return dto;
    }
}

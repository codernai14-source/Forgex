package com.forgex.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.api.dto.EmployeeThirdPartyInvokeDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncResultDTO;
import com.forgex.common.api.feign.IntegrationEmployeeSyncFeignClient;
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
 * 人员同步内部编排控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@RestController
@RequestMapping("/internal/employee")
@RequiredArgsConstructor
public class InternalEmployeeIntegrationController {

    private final IntegrationFacade integrationFacade;
    private final IntegrationEmployeeSyncFeignClient integrationEmployeeSyncFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * 出站同步人员全量主数据。
     *
     * @param request 接口平台调用参数
     * @return 同步结果
     */
    @PostMapping("/sync")
    public R<EmployeeThirdPartySyncResultDTO> syncEmployees(@RequestBody EmployeeThirdPartyInvokeDTO request) {
        EmployeeThirdPartyInvokeDTO safeRequest = request == null ? new EmployeeThirdPartyInvokeDTO() : request;
        List<EmployeeThirdPartySyncDTO> employees = exportEmployees(safeRequest.getTenantId());
        if (employees.isEmpty()) {
            return R.ok(new EmployeeThirdPartySyncResultDTO());
        }
        try {
            TenantContext.set(safeRequest.getTenantId());
            IntegrationExecuteResult result = integrationFacade.invoke(safeRequest.getApiCode(), buildSyncPayload(safeRequest, employees));
            return R.ok(toSyncResult(result, employees.size()));
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 从第三方拉取人员主数据并写入基础数据模块。
     *
     * @param request 接口平台调用参数
     * @return 写入结果
     */
    @PostMapping("/pull")
    public R<EmployeeThirdPartySyncResultDTO> pullEmployees(@RequestBody EmployeeThirdPartyInvokeDTO request) {
        EmployeeThirdPartyInvokeDTO safeRequest = request == null ? new EmployeeThirdPartyInvokeDTO() : request;
        try {
            TenantContext.set(safeRequest.getTenantId());
            IntegrationExecuteResult result = integrationFacade.invoke(safeRequest.getApiCode(), safeRequest.getPayload());
            EmployeeThirdPartySyncRequestDTO syncRequest = new EmployeeThirdPartySyncRequestDTO();
            syncRequest.setTenantId(safeRequest.getTenantId());
            syncRequest.setEmployees(extractEmployees(result == null ? null : result.getData()));
            return integrationEmployeeSyncFeignClient.syncThirdPartyEmployees(syncRequest);
        } finally {
            TenantContext.clear();
        }
    }

    private List<EmployeeThirdPartySyncDTO> exportEmployees(Long tenantId) {
        EmployeeThirdPartySyncRequestDTO request = new EmployeeThirdPartySyncRequestDTO();
        request.setTenantId(tenantId);
        request.setEmployees(Collections.emptyList());
        R<List<EmployeeThirdPartySyncDTO>> response = integrationEmployeeSyncFeignClient.exportThirdPartyEmployees(request);
        return response == null || response.getData() == null ? Collections.emptyList() : response.getData();
    }

    private Map<String, Object> buildSyncPayload(EmployeeThirdPartyInvokeDTO request, List<EmployeeThirdPartySyncDTO> employees) {
        Map<String, Object> payload = request.getPayload();
        if (payload == null) {
            payload = new LinkedHashMap<>();
            request.setPayload(payload);
        }
        payload.put("tenantId", request.getTenantId());
        payload.put("employees", employees);
        return payload;
    }

    private List<EmployeeThirdPartySyncDTO> extractEmployees(Object data) {
        Object normalized = unwrapResponseData(data);
        if (normalized instanceof String text) {
            normalized = parseJsonString(text);
        }
        if (normalized instanceof List<?> list) {
            return list.stream().map(this::convertEmployee).filter(java.util.Objects::nonNull).toList();
        }
        if (normalized instanceof Map<?, ?> map) {
            Object employees = map.get("employees");
            if (employees instanceof String text) {
                employees = parseJsonString(text);
            }
            if (employees instanceof List<?> list) {
                return list.stream().map(this::convertEmployee).filter(java.util.Objects::nonNull).toList();
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

    private EmployeeThirdPartySyncDTO convertEmployee(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof EmployeeThirdPartySyncDTO dto) {
            return dto;
        }
        return objectMapper.convertValue(source, EmployeeThirdPartySyncDTO.class);
    }

    private EmployeeThirdPartySyncResultDTO toSyncResult(IntegrationExecuteResult result, int totalCount) {
        EmployeeThirdPartySyncResultDTO dto = new EmployeeThirdPartySyncResultDTO();
        dto.setTotalCount(totalCount);
        if (result != null && result.isSuccess()) {
            dto.setUpdatedCount(totalCount);
        }
        return dto;
    }
}

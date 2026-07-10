package com.forgex.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.api.dto.CustomerAggregateDTO;
import com.forgex.common.api.dto.CustomerThirdPartyInvokeDTO;
import com.forgex.common.api.dto.CustomerThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.CustomerThirdPartySyncResultDTO;
import com.forgex.common.api.feign.IntegrationCustomerSyncFeignClient;
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
 * 客户同步内部编排控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@RestController
@RequestMapping("/internal/customer")
@RequiredArgsConstructor
public class InternalCustomerIntegrationController {

    private final IntegrationFacade integrationFacade;
    private final IntegrationCustomerSyncFeignClient integrationCustomerSyncFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * 出站同步客户主数据。
     *
     * @param request 接口平台调用参数
     * @return 同步结果
     */
    @PostMapping("/sync")
    public R<CustomerThirdPartySyncResultDTO> syncCustomers(@RequestBody CustomerThirdPartyInvokeDTO request) {
        CustomerThirdPartyInvokeDTO safeRequest = request == null ? new CustomerThirdPartyInvokeDTO() : request;
        List<CustomerAggregateDTO> customers = exportCustomers(safeRequest.getTenantId());
        if (customers.isEmpty()) {
            CustomerThirdPartySyncResultDTO empty = new CustomerThirdPartySyncResultDTO();
            empty.setTotalCount(0);
            return R.ok(empty);
        }
        try {
            TenantContext.set(safeRequest.getTenantId());
            IntegrationExecuteResult result = integrationFacade.invoke(safeRequest.getApiCode(), buildSyncPayload(safeRequest, customers));
            return R.ok(toSyncResult(result, customers.size()));
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 从第三方拉取客户主数据并写入基础数据模块。
     *
     * @param request 接口平台调用参数
     * @return 写入结果
     */
    @PostMapping("/pull")
    public R<CustomerThirdPartySyncResultDTO> pullCustomers(@RequestBody CustomerThirdPartyInvokeDTO request) {
        CustomerThirdPartyInvokeDTO safeRequest = request == null ? new CustomerThirdPartyInvokeDTO() : request;
        try {
            TenantContext.set(safeRequest.getTenantId());
            IntegrationExecuteResult result = integrationFacade.invoke(safeRequest.getApiCode(), safeRequest.getPayload());
            CustomerThirdPartySyncRequestDTO syncRequest = new CustomerThirdPartySyncRequestDTO();
            syncRequest.setTenantId(safeRequest.getTenantId());
            syncRequest.setCustomers(extractCustomers(result == null ? null : result.getData()));
            return integrationCustomerSyncFeignClient.syncThirdPartyCustomers(syncRequest);
        } finally {
            TenantContext.clear();
        }
    }

    private List<CustomerAggregateDTO> exportCustomers(Long tenantId) {
        CustomerThirdPartySyncRequestDTO request = new CustomerThirdPartySyncRequestDTO();
        request.setTenantId(tenantId);
        request.setCustomers(Collections.emptyList());
        R<List<CustomerAggregateDTO>> response = integrationCustomerSyncFeignClient.exportThirdPartyCustomers(request);
        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }
        return response.getData();
    }

    private Map<String, Object> buildSyncPayload(CustomerThirdPartyInvokeDTO request, List<CustomerAggregateDTO> customers) {
        Map<String, Object> payload = request.getPayload();
        if (payload == null) {
            payload = new LinkedHashMap<>();
            request.setPayload(payload);
        }
        payload.put("tenantId", request.getTenantId());
        payload.put("customers", customers);
        return payload;
    }

    @SuppressWarnings("unchecked")
    private List<CustomerAggregateDTO> extractCustomers(Object data) {
        if (data instanceof List<?>) {
            return ((List<?>) data).stream()
                    .map(this::convertCustomer)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        if (data instanceof Map<?, ?> map && map.get("customers") instanceof List<?>) {
            return ((List<?>) map.get("customers")).stream()
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

    private CustomerThirdPartySyncResultDTO toSyncResult(IntegrationExecuteResult result, int totalCount) {
        CustomerThirdPartySyncResultDTO dto = new CustomerThirdPartySyncResultDTO();
        dto.setTotalCount(totalCount);
        if (result != null && result.isSuccess()) {
            dto.setUpdatedCount(totalCount);
        }
        return dto;
    }
}

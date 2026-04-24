package com.forgex.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ApiConfigDTO;
import com.forgex.integration.domain.dto.ApiTaskResultDTO;
import com.forgex.integration.domain.entity.ApiCallLog;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.domain.model.ApiTaskSubmitResult;
import com.forgex.integration.domain.model.IntegrationExecuteResult;
import com.forgex.integration.domain.model.OutboundRequestDefinition;
import com.forgex.integration.enums.ApiInvokeModeEnum;
import com.forgex.integration.enums.ApiResultTypeEnum;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.service.IApiDefinitionService;
import com.forgex.integration.service.IApiGatewayService;
import com.forgex.integration.service.IApiLogBufferService;
import com.forgex.integration.service.IApiOutboundExecutor;
import com.forgex.integration.service.IApiParamAssembler;
import com.forgex.integration.service.IApiTaskResultService;
import com.forgex.integration.service.IApiTaskService;
import com.forgex.integration.service.IThirdAuthorizationService;
import com.forgex.integration.spi.ApiInboundInterpreter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 缁熶竴鍏ュ彛鎵ц鏈嶅姟
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiGatewayServiceImpl implements IApiGatewayService {

    private final ObjectMapper objectMapper;
    private final IApiDefinitionService apiDefinitionService;
    private final IApiParamAssembler apiParamAssembler;
    private final IApiOutboundExecutor apiOutboundExecutor;
    private final IApiTaskService apiTaskService;
    private final IApiTaskResultService apiTaskResultService;
    private final IApiLogBufferService apiLogBufferService;
    private final ApiInterpreterRegistry interpreterRegistry;
    private final IThirdAuthorizationService thirdAuthorizationService;
    private final ThreadPoolTaskExecutor integrationSyncExecutor;

    @Override
    public IntegrationExecuteResult invokeInbound(String apiCode, Map<String, Object> rawPayload, String callerIp) {
        ApiDefinitionSnapshot snapshot = apiDefinitionService.getSnapshot(apiCode, "INBOUND");
        if (snapshot == null || snapshot.getApiConfig() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND, apiCode);
        }
        String token = rawPayload == null ? null : String.valueOf(rawPayload.getOrDefault("token", ""));
        if (token != null && !token.isBlank() && !thirdAuthorizationService.validateToken(token)) {
            return IntegrationExecuteResult.builder()
                .accepted(false)
                .success(false)
                .status("AUTH_FAIL")
                .resultType(ApiResultTypeEnum.AUTH_FAIL.name())
                .errorMessage("authorization failed")
                .build();
        }
        ApiExecutionContext context = newContext(snapshot.getApiConfig(), callerIp, null);
        Map<String, Object> assembled = apiParamAssembler.assembleInbound(snapshot, rawPayload);
        ApiInvokeModeEnum mode = ApiInvokeModeEnum.fromValue(snapshot.getApiConfig().getInvokeMode());
        if (mode == ApiInvokeModeEnum.ASYNC) {
            ApiTaskSubmitResult submitResult = apiTaskService.submit(snapshot, context, rawPayload, assembled);
            writeLog(snapshot.getApiConfig(), context, rawPayload, assembled, null,
                ApiResultTypeEnum.SUCCESS.name(), "WAITING", null, 0);
            return IntegrationExecuteResult.builder()
                .accepted(true)
                .success(true)
                .taskId(submitResult.getTaskId())
                .traceId(submitResult.getTraceId())
                .status(submitResult.getStatus())
                .resultType(ApiResultTypeEnum.SUCCESS.name())
                .build();
        }
        return executeInboundSync(snapshot, context, rawPayload, assembled);
    }

    @Override
    public IntegrationExecuteResult invokeOutbound(String apiCode, Object requestEntity) {
        ApiDefinitionSnapshot snapshot = apiDefinitionService.getSnapshot(apiCode, "OUTBOUND");
        if (snapshot == null || snapshot.getApiConfig() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND, apiCode);
        }
        Map<String, Object> rawPayload = objectMapper.convertValue(requestEntity, Map.class);
        ApiExecutionContext context = newContext(snapshot.getApiConfig(), null, null);
        OutboundRequestDefinition assembled = apiParamAssembler.assembleOutbound(snapshot, rawPayload);
        long start = System.currentTimeMillis();
        try {
            Object result = apiOutboundExecutor.execute(snapshot, context, assembled);
            writeLog(snapshot.getApiConfig(), context, rawPayload, assembled.getBody(), result,
                ApiResultTypeEnum.SUCCESS.name(), "SUCCESS", null, (int) (System.currentTimeMillis() - start));
            return IntegrationExecuteResult.builder()
                .accepted(true)
                .success(true)
                .traceId(context.getTraceId())
                .status("SUCCESS")
                .resultType(ApiResultTypeEnum.SUCCESS.name())
                .data(result)
                .build();
        } catch (Exception ex) {
            writeLog(snapshot.getApiConfig(), context, rawPayload, assembled.getBody(), null,
                ApiResultTypeEnum.SYSTEM_FAIL.name(), "FAIL", ex.getMessage(), (int) (System.currentTimeMillis() - start));
            throw ex;
        }
    }

    public ApiTaskResultDTO getTaskResult(String taskId) {
        return apiTaskResultService.getByTaskId(taskId);
    }

    private IntegrationExecuteResult executeInboundSync(ApiDefinitionSnapshot snapshot,
                                                        ApiExecutionContext context,
                                                        Map<String, Object> rawPayload,
                                                        Map<String, Object> assembledPayload) {
        long start = System.currentTimeMillis();
        try {
            ApiInboundInterpreter interpreter = interpreterRegistry.get(snapshot.getApiConfig().getProcessorBean());
            if (interpreter == null) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR,
                    IntegrationPromptEnum.API_HANDLER_NOT_CONFIGURED,
                    snapshot.getApiConfig().getProcessorBean());
            }
            Object result = integrationSyncExecutor.submit(() -> interpreter.handle(context, assembledPayload)).get();
            writeLog(snapshot.getApiConfig(), context, rawPayload, assembledPayload, result,
                ApiResultTypeEnum.SUCCESS.name(), "SUCCESS", null, (int) (System.currentTimeMillis() - start));
            return IntegrationExecuteResult.builder()
                .accepted(true)
                .success(true)
                .traceId(context.getTraceId())
                .status("SUCCESS")
                .resultType(ApiResultTypeEnum.SUCCESS.name())
                .data(result)
                .build();
        } catch (Exception ex) {
            writeLog(snapshot.getApiConfig(), context, rawPayload, assembledPayload, null,
                ApiResultTypeEnum.SYSTEM_FAIL.name(), "FAIL", ex.getMessage(), (int) (System.currentTimeMillis() - start));
            throw new IllegalStateException("execute inbound sync failed", ex);
        }
    }

    private ApiExecutionContext newContext(ApiConfigDTO config, String callerIp, String taskId) {
        Long tenantId = TenantContext.get();
        return ApiExecutionContext.builder()
            .tenantId(tenantId == null ? 0L : tenantId)
            .apiConfigId(config.getId())
            .apiCode(config.getApiCode())
            .direction(config.getDirection())
            .traceId(UUID.randomUUID().toString().replace("-", ""))
            .taskId(taskId)
            .callerIp(callerIp)
            .invokeMode(config.getInvokeMode())
            .startTime(LocalDateTime.now())
            .build();
    }

    private void writeLog(ApiConfigDTO config,
                          ApiExecutionContext context,
                          Object rawPayload,
                          Object assembledPayload,
                          Object result,
                          String resultType,
                          String callStatus,
                          String errorMessage,
                          int costTimeMs) {
        try {
            ApiCallLog logEntity = new ApiCallLog();
            logEntity.setTenantId(context.getTenantId());
            logEntity.setApiConfigId(config.getId());
            logEntity.setApiCode(config.getApiCode());
            logEntity.setCallDirection(config.getDirection());
            logEntity.setCallerIp(context.getCallerIp());
            logEntity.setTraceId(context.getTraceId());
            logEntity.setTaskId(context.getTaskId());
            logEntity.setInvokeMode(context.getInvokeMode());
            logEntity.setRawRequestData(objectMapper.writeValueAsString(rawPayload));
            logEntity.setRequestData(objectMapper.writeValueAsString(rawPayload));
            logEntity.setAssembledRequestData(objectMapper.writeValueAsString(assembledPayload));
            logEntity.setResponseData(result == null ? null : objectMapper.writeValueAsString(result));
            logEntity.setResponseCode("200");
            logEntity.setCallStatus(callStatus);
            logEntity.setResultType(resultType);
            logEntity.setErrorMessage(errorMessage);
            logEntity.setCostTimeMs(costTimeMs);
            logEntity.setCallTime(LocalDateTime.now());
            apiLogBufferService.buffer(logEntity);
        } catch (Exception ex) {
            log.warn("buffer integration log failed", ex);
        }
    }
}

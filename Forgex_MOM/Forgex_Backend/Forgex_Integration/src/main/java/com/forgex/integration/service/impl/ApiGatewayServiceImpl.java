package com.forgex.integration.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ApiConfigDTO;
import com.forgex.integration.domain.dto.ApiOutboundTargetDTO;
import com.forgex.integration.domain.dto.ApiTaskResultDTO;
import com.forgex.integration.domain.entity.ApiCallLog;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.domain.model.ApiTaskSubmitResult;
import com.forgex.integration.domain.model.IntegrationExecuteResult;
import com.forgex.integration.domain.model.IntegrationTargetResult;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 集成接口网关服务实现。
 * <p>
 * 负责入站接口调用、出站接口调用、同步执行、异步任务提交、执行日志缓冲以及任务结果查询。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see IApiGatewayService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiGatewayServiceImpl implements IApiGatewayService {

    /**
     * JSON 序列化器。
     */
    private final ObjectMapper objectMapper;

    /**
     * 接口定义服务。
     */
    private final IApiDefinitionService apiDefinitionService;

    /**
     * 参数组装服务。
     */
    private final IApiParamAssembler apiParamAssembler;

    /**
     * 出站执行器。
     */
    private final IApiOutboundExecutor apiOutboundExecutor;

    /**
     * 异步任务服务。
     */
    private final IApiTaskService apiTaskService;

    /**
     * 异步任务结果服务。
     */
    private final IApiTaskResultService apiTaskResultService;

    /**
     * 接口调用日志缓冲服务。
     */
    private final IApiLogBufferService apiLogBufferService;

    /**
     * 入站处理器注册表。
     */
    private final ApiInterpreterRegistry interpreterRegistry;

    /**
     * 第三方授权服务。
     */
    private final IThirdAuthorizationService thirdAuthorizationService;

    /**
     * 集成同步执行线程池。
     */
    private final ThreadPoolTaskExecutor integrationSyncExecutor;

    /**
     * 调用入站接口。
     *
     * @param apiCode    接口编码
     * @param rawPayload 原始参数
     * @param callerIp   调用方 IP
     * @return 执行结果
     */
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
        ApiExecutionContext context = newContext(snapshot.getApiConfig(), null, null, callerIp, null);
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
                .asyncResults(List.of(toTargetResult(submitResult)))
                .build();
        }
        return executeInboundSync(snapshot, context, rawPayload, assembled);
    }

    /**
     * 调用出站接口。
     *
     * @param apiCode       接口编码
     * @param requestEntity 请求实体
     * @return 执行结果
     */
    @Override
    public IntegrationExecuteResult invokeOutbound(String apiCode, Object requestEntity) {
        ApiDefinitionSnapshot snapshot = apiDefinitionService.getSnapshot(apiCode, "OUTBOUND");
        if (snapshot == null || snapshot.getApiConfig() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND, apiCode);
        }
        Map<String, Object> rawPayload = objectMapper.convertValue(requestEntity, new TypeReference<>() {});
        List<ApiOutboundTargetDTO> targets = snapshot.getOutboundTargets();
        if (targets == null || targets.isEmpty()) {
            return invokeOutboundLegacy(snapshot, rawPayload);
        }

        List<IntegrationTargetResult> syncResults = new ArrayList<>();
        List<IntegrationTargetResult> asyncResults = new ArrayList<>();

        for (ApiOutboundTargetDTO target : targets) {
            ApiExecutionContext context = newContext(snapshot.getApiConfig(), target, null, null, null);
            OutboundRequestDefinition assembled = apiParamAssembler.assembleOutbound(snapshot, rawPayload, target);
            ApiInvokeModeEnum mode = ApiInvokeModeEnum.fromValue(target.getInvokeMode());
            if (mode == ApiInvokeModeEnum.ASYNC) {
                ApiTaskSubmitResult submitResult = apiTaskService.submit(snapshot, context, rawPayload, assembled);
                writeLog(snapshot.getApiConfig(), context, rawPayload, assembled.getBody(), null,
                    ApiResultTypeEnum.SUCCESS.name(), "WAITING", null, 0);
                asyncResults.add(toTargetResult(submitResult));
            } else {
                syncResults.add(executeOutboundSync(snapshot, context, rawPayload, assembled));
            }
        }

        boolean success = syncResults.stream().allMatch(IntegrationTargetResult::isSuccess);
        boolean accepted = !syncResults.isEmpty() || !asyncResults.isEmpty();
        String status = !syncResults.isEmpty() ? (success ? "SUCCESS" : "PARTIAL_FAIL") : "WAITING";
        return IntegrationExecuteResult.builder()
            .accepted(accepted)
            .success(success)
            .status(status)
            .resultType(success ? ApiResultTypeEnum.SUCCESS.name() : ApiResultTypeEnum.SYSTEM_FAIL.name())
            .syncResults(syncResults)
            .asyncResults(asyncResults)
            .data(syncResults.size() == 1 ? syncResults.get(0).getData() : syncResults)
            .build();
    }

    /**
     * 查询异步任务结果。
     *
     * @param taskId 任务 ID
     * @return 任务结果
     */
    public ApiTaskResultDTO getTaskResult(String taskId) {
        return apiTaskResultService.getByTaskId(taskId);
    }

    /**
     * 执行异步任务。
     *
     * @param snapshot 接口定义快照
     * @param context  执行上下文
     * @param payload  任务载荷
     * @return 执行结果
     * @throws Exception 执行异常
     */
    public Object executeTask(ApiDefinitionSnapshot snapshot, ApiExecutionContext context, ApiTaskPayload payload) throws Exception {
        if ("OUTBOUND".equalsIgnoreCase(context.getDirection())) {
            OutboundRequestDefinition requestDefinition = payload.outboundRequest();
            return apiOutboundExecutor.execute(snapshot, context, requestDefinition);
        }
        ApiInboundInterpreter interpreter = interpreterRegistry.get(snapshot.getApiConfig().getProcessorBean());
        if (interpreter == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR,
                IntegrationPromptEnum.API_HANDLER_NOT_CONFIGURED, snapshot.getApiConfig().getProcessorBean());
        }
        return interpreter.handle(context, payload.inboundPayload());
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
                    IntegrationPromptEnum.API_HANDLER_NOT_CONFIGURED, snapshot.getApiConfig().getProcessorBean());
            }
            Object result = integrationSyncExecutor.submit(() -> interpreter.handle(context, assembledPayload)).get();
            writeLog(snapshot.getApiConfig(), context, rawPayload, assembledPayload, result,
                ApiResultTypeEnum.SUCCESS.name(), "SUCCESS", null, (int) (System.currentTimeMillis() - start));
            IntegrationTargetResult targetResult = IntegrationTargetResult.builder()
                .accepted(true)
                .success(true)
                .traceId(context.getTraceId())
                .status("SUCCESS")
                .resultType(ApiResultTypeEnum.SUCCESS.name())
                .data(result)
                .build();
            return IntegrationExecuteResult.builder()
                .accepted(true)
                .success(true)
                .traceId(context.getTraceId())
                .status("SUCCESS")
                .resultType(ApiResultTypeEnum.SUCCESS.name())
                .data(result)
                .syncResults(List.of(targetResult))
                .build();
        } catch (Exception ex) {
            writeLog(snapshot.getApiConfig(), context, rawPayload, assembledPayload, null,
                ApiResultTypeEnum.SYSTEM_FAIL.name(), "FAIL", ex.getMessage(), (int) (System.currentTimeMillis() - start));
            throw new IllegalStateException("execute inbound sync failed", ex);
        }
    }

    private IntegrationTargetResult executeOutboundSync(ApiDefinitionSnapshot snapshot,
                                                        ApiExecutionContext context,
                                                        Map<String, Object> rawPayload,
                                                        OutboundRequestDefinition assembled) {
        long start = System.currentTimeMillis();
        try {
            Object result = apiOutboundExecutor.execute(snapshot, context, assembled);
            writeLog(snapshot.getApiConfig(), context, rawPayload, assembled.getBody(), result,
                ApiResultTypeEnum.SUCCESS.name(), "SUCCESS", null, (int) (System.currentTimeMillis() - start));
            return IntegrationTargetResult.builder()
                .accepted(true)
                .success(true)
                .outboundTargetId(context.getOutboundTargetId())
                .targetSystemCode(context.getTargetSystemCode())
                .targetSystemName(context.getTargetSystemName())
                .traceId(context.getTraceId())
                .status("SUCCESS")
                .resultType(ApiResultTypeEnum.SUCCESS.name())
                .data(result)
                .build();
        } catch (Exception ex) {
            writeLog(snapshot.getApiConfig(), context, rawPayload, assembled.getBody(), null,
                ApiResultTypeEnum.SYSTEM_FAIL.name(), "FAIL", ex.getMessage(), (int) (System.currentTimeMillis() - start));
            return IntegrationTargetResult.builder()
                .accepted(true)
                .success(false)
                .outboundTargetId(context.getOutboundTargetId())
                .targetSystemCode(context.getTargetSystemCode())
                .targetSystemName(context.getTargetSystemName())
                .traceId(context.getTraceId())
                .status("FAIL")
                .resultType(ApiResultTypeEnum.SYSTEM_FAIL.name())
                .errorMessage(ex.getMessage())
                .build();
        }
    }

    private IntegrationExecuteResult invokeOutboundLegacy(ApiDefinitionSnapshot snapshot, Map<String, Object> rawPayload) {
        ApiExecutionContext context = newContext(snapshot.getApiConfig(), null, null, null, null);
        OutboundRequestDefinition assembled = apiParamAssembler.assembleOutbound(snapshot, rawPayload);
        IntegrationTargetResult result = executeOutboundSync(snapshot, context, rawPayload, assembled);
        return IntegrationExecuteResult.builder()
            .accepted(result.isAccepted())
            .success(result.isSuccess())
            .traceId(result.getTraceId())
            .status(result.getStatus())
            .resultType(result.getResultType())
            .data(result.getData())
            .syncResults(List.of(result))
            .build();
    }

    private ApiExecutionContext newContext(ApiConfigDTO config,
                                           ApiOutboundTargetDTO target,
                                           String taskId,
                                           String callerIp,
                                           String traceId) {
        Long tenantId = TenantContext.get();
        return ApiExecutionContext.builder()
            .tenantId(tenantId == null ? 0L : tenantId)
            .apiConfigId(config.getId())
            .outboundTargetId(target == null ? null : target.getId())
            .apiCode(config.getApiCode())
            .targetSystemCode(target == null ? null : target.getTargetCode())
            .targetSystemName(target == null ? null : target.getTargetName())
            .direction(config.getDirection())
            .traceId(traceId == null ? UUID.randomUUID().toString().replace("-", "") : traceId)
            .taskId(taskId)
            .callerIp(callerIp)
            .invokeMode(target == null ? config.getInvokeMode() : target.getInvokeMode())
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
            logEntity.setOutboundTargetId(context.getOutboundTargetId());
            logEntity.setTargetSystemCode(context.getTargetSystemCode());
            logEntity.setTargetSystemName(context.getTargetSystemName());
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

    private IntegrationTargetResult toTargetResult(ApiTaskSubmitResult submitResult) {
        return IntegrationTargetResult.builder()
            .accepted(true)
            .success(true)
            .outboundTargetId(submitResult.getOutboundTargetId())
            .targetSystemCode(submitResult.getTargetSystemCode())
            .targetSystemName(submitResult.getTargetSystemName())
            .taskId(submitResult.getTaskId())
            .traceId(submitResult.getTraceId())
            .status(submitResult.getStatus())
            .resultType(ApiResultTypeEnum.SUCCESS.name())
            .build();
    }

    /**
     * 异步任务执行载荷。
     *
     * @param inboundPayload  入站参数
     * @param outboundRequest 出站请求定义
     */
    public record ApiTaskPayload(Map<String, Object> inboundPayload, OutboundRequestDefinition outboundRequest) {
    }
}

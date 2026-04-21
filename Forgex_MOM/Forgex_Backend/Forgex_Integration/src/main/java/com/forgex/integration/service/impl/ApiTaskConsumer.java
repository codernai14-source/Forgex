package com.forgex.integration.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.integration.domain.entity.ApiTask;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.service.IApiDefinitionService;
import com.forgex.integration.service.IApiTaskService;
import com.forgex.integration.spi.ApiInboundInterpreter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 寮傛浠诲姟娑堣垂鑰?
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiTaskConsumer {

    private final ApiTaskServiceImpl.TaskQueueBridge taskQueueBridge;
    private final IApiTaskService apiTaskService;
    private final IApiDefinitionService apiDefinitionService;
    private final ApiInterpreterRegistry interpreterRegistry;
    private final ObjectMapper objectMapper;
    private final ThreadPoolTaskExecutor integrationAsyncConsumerExecutor;

    @Scheduled(fixedDelay = 300)
    public void consume() {
        for (int i = 0; i < 20; i++) {
            String taskId = taskQueueBridge.poll();
            if (taskId == null) {
                return;
            }
            integrationAsyncConsumerExecutor.execute(() -> process(taskId));
        }
    }

    private void process(String taskId) {
        apiTaskService.claim(taskId).ifPresent(task -> {
            Long previousTenant = TenantContext.get();
            try {
                TenantContext.set(task.getTenantId());
                long start = System.currentTimeMillis();
                ApiDefinitionSnapshot snapshot = apiDefinitionService.getSnapshot(task.getApiCode(), task.getDirection());
                ApiInboundInterpreter interpreter = interpreterRegistry.get(task.getProcessorBean());
                Map<String, Object> assembled = objectMapper.readValue(task.getAssembledPayload(), new TypeReference<>() {});
                ApiExecutionContext context = ApiExecutionContext.builder()
                    .tenantId(task.getTenantId())
                    .apiConfigId(task.getApiConfigId())
                    .apiCode(task.getApiCode())
                    .direction(task.getDirection())
                    .traceId(task.getTraceId())
                    .taskId(task.getTaskId())
                    .invokeMode(task.getInvokeMode())
                    .startTime(LocalDateTime.now())
                    .build();
                Object result = interpreter.handle(context, assembled);
                apiTaskService.markSuccess(task, result, (int) (System.currentTimeMillis() - start));
            } catch (Exception ex) {
                log.error("consume api task failed, taskId={}", taskId, ex);
                apiTaskService.markFailure(task, "SYSTEM_FAIL", ex.getMessage(), 0);
            } finally {
                if (previousTenant != null) {
                    TenantContext.set(previousTenant);
                } else {
                    TenantContext.clear();
                }
            }
        });
    }
}

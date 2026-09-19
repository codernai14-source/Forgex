package com.forgex.integration.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.integration.domain.entity.ApiTask;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.domain.model.OutboundRequestDefinition;
import com.forgex.integration.service.IApiDefinitionService;
import com.forgex.integration.service.IApiTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 接口异步任务消费者。
 * <p>
 * 定时从 Redis 队列拉取任务 ID，抢占任务后交给集成异步线程池执行，并在任务执行前后维护租户上下文。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiTaskConsumer {

    /**
     * Redis 队列桥接器。
     */
    private final ApiTaskServiceImpl.TaskQueueBridge taskQueueBridge;

    /**
     * 接口异步任务服务。
     */
    private final IApiTaskService apiTaskService;

    /**
     * 接口定义服务。
     */
    private final IApiDefinitionService apiDefinitionService;

    /**
     * 接口网关服务。
     */
    private final ApiGatewayServiceImpl apiGatewayService;

    /**
     * JSON 序列化器。
     */
    private final ObjectMapper objectMapper;

    /**
     * 集成异步消费线程池。
     */
    private final ThreadPoolTaskExecutor integrationAsyncConsumerExecutor;

    /**
     * 定时消费任务队列。
     */
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

    /**
     * 处理单个任务。
     *
     * @param taskId 任务 ID
     */
    private void process(String taskId) {
        apiTaskService.claim(taskId).ifPresent(task -> {
            Long previousTenant = TenantContext.get();
            try {
                TenantContext.set(task.getTenantId());
                long start = System.currentTimeMillis();
                ApiDefinitionSnapshot snapshot = apiDefinitionService.getSnapshot(task.getApiCode(), task.getDirection());
                ApiExecutionContext context = ApiExecutionContext.builder()
                    .tenantId(task.getTenantId())
                    .apiConfigId(task.getApiConfigId())
                    .outboundTargetId(task.getOutboundTargetId())
                    .apiCode(task.getApiCode())
                    .targetSystemCode(task.getTargetSystemCode())
                    .targetSystemName(task.getTargetSystemName())
                    .direction(task.getDirection())
                    .traceId(task.getTraceId())
                    .taskId(task.getTaskId())
                    .invokeMode(task.getInvokeMode())
                    .startTime(LocalDateTime.now())
                    .build();

                ApiGatewayServiceImpl.ApiTaskPayload payload = "OUTBOUND".equalsIgnoreCase(task.getDirection())
                    ? new ApiGatewayServiceImpl.ApiTaskPayload(
                        null,
                        objectMapper.readValue(task.getAssembledPayload(), OutboundRequestDefinition.class))
                    : new ApiGatewayServiceImpl.ApiTaskPayload(
                        objectMapper.readValue(task.getAssembledPayload(), new TypeReference<Map<String, Object>>() {}),
                        null);

                Object result = apiGatewayService.executeTask(snapshot, context, payload);
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

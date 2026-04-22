package com.forgex.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.integration.config.IntegrationProperties;
import com.forgex.integration.domain.entity.ApiTask;
import com.forgex.integration.domain.entity.ApiTaskResult;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.domain.model.ApiTaskSubmitResult;
import com.forgex.integration.domain.model.OutboundRequestDefinition;
import com.forgex.integration.enums.ApiTaskStatusEnum;
import com.forgex.integration.mapper.ApiTaskMapper;
import com.forgex.integration.service.IApiTaskResultService;
import com.forgex.integration.service.IApiTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 接口异步任务服务实现。
 * <p>
 * 负责提交异步接口任务、抢占任务、标记成功/失败、失败重试和超时任务补偿入队。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see IApiTaskService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiTaskServiceImpl extends ServiceImpl<ApiTaskMapper, ApiTask> implements IApiTaskService {

    /**
     * JSON 序列化器。
     */
    private final ObjectMapper objectMapper;

    /**
     * 异步任务结果服务。
     */
    private final IApiTaskResultService apiTaskResultService;

    /**
     * 集成平台配置。
     */
    private final IntegrationProperties properties;

    /**
     * Redis 队列桥接器。
     */
    private final TaskQueueBridge taskQueueBridge;

    /**
     * 提交接口异步任务。
     *
     * @param snapshot         接口定义快照
     * @param context          执行上下文
     * @param rawPayload       原始请求参数
     * @param assembledPayload 组装后的请求参数
     * @return 任务提交结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiTaskSubmitResult submit(ApiDefinitionSnapshot snapshot,
                                      ApiExecutionContext context,
                                      Map<String, Object> rawPayload,
                                      Object assembledPayload) {
        try {
            String taskId = UUID.randomUUID().toString().replace("-", "");
            Integer maxRetryCount = context.getOutboundTargetId() != null
                ? Optional.ofNullable(snapshot.getOutboundTargets()).orElse(List.of()).stream()
                    .filter(item -> context.getOutboundTargetId().equals(item.getId()))
                    .findFirst()
                    .map(item -> item.getRetryCount() == null ? 0 : item.getRetryCount())
                    .orElse(snapshot.getApiConfig().getRetryCount() == null ? 0 : snapshot.getApiConfig().getRetryCount())
                : snapshot.getApiConfig().getRetryCount() == null ? 0 : snapshot.getApiConfig().getRetryCount();
            ApiTask task = new ApiTask();
            task.setTaskId(taskId);
            task.setTraceId(context.getTraceId());
            task.setApiConfigId(snapshot.getApiConfig().getId());
            task.setOutboundTargetId(context.getOutboundTargetId());
            task.setTargetSystemCode(context.getTargetSystemCode());
            task.setTargetSystemName(context.getTargetSystemName());
            task.setApiCode(snapshot.getApiConfig().getApiCode());
            task.setDirection(snapshot.getApiConfig().getDirection());
            task.setProcessorBean(snapshot.getApiConfig().getProcessorBean());
            task.setInvokeMode(context.getInvokeMode());
            task.setRequestPayload(objectMapper.writeValueAsString(rawPayload));
            task.setAssembledPayload(objectMapper.writeValueAsString(assembledPayload));
            task.setStatus(ApiTaskStatusEnum.WAITING.name());
            task.setRetryCount(0);
            task.setMaxRetryCount(maxRetryCount);
            task.setNextExecuteTime(LocalDateTime.now());
            task.setTenantId(context.getTenantId());
            this.save(task);
            enqueue(taskId);
            return ApiTaskSubmitResult.builder()
                .taskId(taskId)
                .traceId(context.getTraceId())
                .status(ApiTaskStatusEnum.WAITING.name())
                .outboundTargetId(context.getOutboundTargetId())
                .targetSystemCode(context.getTargetSystemCode())
                .targetSystemName(context.getTargetSystemName())
                .build();
        } catch (Exception ex) {
            throw new IllegalStateException("submit api task failed", ex);
        }
    }

    /**
     * 抢占待执行任务。
     *
     * @param taskId 任务 ID
     * @return 成功抢占的任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Optional<ApiTask> claim(String taskId) {
        LambdaQueryWrapper<ApiTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiTask::getTaskId, taskId);
        wrapper.eq(ApiTask::getDeleted, false);
        wrapper.last("LIMIT 1");
        ApiTask task = this.getOne(wrapper);
        if (task == null) {
            return Optional.empty();
        }
        if (!(ApiTaskStatusEnum.WAITING.name().equals(task.getStatus())
            || ApiTaskStatusEnum.RETRY.name().equals(task.getStatus())
            || ApiTaskStatusEnum.QUEUED.name().equals(task.getStatus()))) {
            return Optional.empty();
        }
        task.setStatus(ApiTaskStatusEnum.RUNNING.name());
        task.setStartedTime(LocalDateTime.now());
        task.setLeaseExpireTime(LocalDateTime.now().plusSeconds(properties.getQueue().getLeaseSeconds()));
        this.updateById(task);
        return Optional.of(task);
    }

    /**
     * 标记任务成功。
     *
     * @param task       任务实体
     * @param result     执行结果
     * @param costTimeMs 耗时，单位毫秒
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSuccess(ApiTask task, Object result, int costTimeMs) {
        ApiTaskResult taskResult = new ApiTaskResult();
        taskResult.setTaskId(task.getTaskId());
        taskResult.setTraceId(task.getTraceId());
        taskResult.setApiConfigId(task.getApiConfigId());
        taskResult.setOutboundTargetId(task.getOutboundTargetId());
        taskResult.setTargetSystemCode(task.getTargetSystemCode());
        taskResult.setTargetSystemName(task.getTargetSystemName());
        taskResult.setApiCode(task.getApiCode());
        taskResult.setDirection(task.getDirection());
        taskResult.setStatus(ApiTaskStatusEnum.SUCCESS.name());
        taskResult.setResultType("SUCCESS");
        taskResult.setResultData(String.valueOf(result));
        taskResult.setCostTimeMs(costTimeMs);
        taskResult.setFinishedTime(LocalDateTime.now());
        taskResult.setExpireTime(LocalDateTime.now().plusDays(properties.getAsyncResultRetentionDays()));
        taskResult.setTenantId(task.getTenantId());
        apiTaskResultService.save(taskResult);
        this.removeById(task.getId());
    }

    /**
     * 标记任务失败。
     * <p>
     * 仍有重试次数时重新入队，否则写入最终失败结果并删除任务。
     * </p>
     *
     * @param task         任务实体
     * @param resultType   结果类型
     * @param errorMessage 错误信息
     * @param costTimeMs   耗时，单位毫秒
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailure(ApiTask task, String resultType, String errorMessage, int costTimeMs) {
        boolean canRetry = task.getRetryCount() != null
            && task.getMaxRetryCount() != null
            && task.getRetryCount() < task.getMaxRetryCount();
        if (canRetry) {
            int nextRetryCount = task.getRetryCount() + 1;
            task.setRetryCount(nextRetryCount);
            task.setStatus(ApiTaskStatusEnum.RETRY.name());
            task.setNextExecuteTime(LocalDateTime.now().plusNanos(
                Math.max(1000L, nextRetryCount * 1000L) * 1_000_000L));
            task.setErrorMessage(errorMessage);
            task.setResultType(resultType);
            this.updateById(task);
            enqueue(task.getTaskId());
            return;
        }

        ApiTaskResult taskResult = new ApiTaskResult();
        taskResult.setTaskId(task.getTaskId());
        taskResult.setTraceId(task.getTraceId());
        taskResult.setApiConfigId(task.getApiConfigId());
        taskResult.setOutboundTargetId(task.getOutboundTargetId());
        taskResult.setTargetSystemCode(task.getTargetSystemCode());
        taskResult.setTargetSystemName(task.getTargetSystemName());
        taskResult.setApiCode(task.getApiCode());
        taskResult.setDirection(task.getDirection());
        taskResult.setStatus(ApiTaskStatusEnum.FAIL.name());
        taskResult.setResultType(resultType);
        taskResult.setErrorMessage(errorMessage);
        taskResult.setCostTimeMs(costTimeMs);
        taskResult.setFinishedTime(LocalDateTime.now());
        taskResult.setExpireTime(LocalDateTime.now().plusDays(properties.getAsyncResultRetentionDays()));
        taskResult.setTenantId(task.getTenantId());
        apiTaskResultService.save(taskResult);
        this.removeById(task.getId());
    }

    /**
     * 补偿重新入队过期任务。
     */
    @Override
    @Scheduled(fixedDelay = 5000)
    public void requeueExpiredTasks() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<ApiTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiTask::getDeleted, false);
        wrapper.and(item -> item
            .and(w -> w.eq(ApiTask::getStatus, ApiTaskStatusEnum.WAITING.name())
                .le(ApiTask::getNextExecuteTime, now))
            .or(w -> w.eq(ApiTask::getStatus, ApiTaskStatusEnum.RETRY.name())
                .le(ApiTask::getNextExecuteTime, now))
            .or(w -> w.eq(ApiTask::getStatus, ApiTaskStatusEnum.RUNNING.name())
                .le(ApiTask::getLeaseExpireTime, now)));
        wrapper.last("LIMIT " + properties.getQueue().getCompensateLimit());
        List<ApiTask> tasks = this.list(wrapper);
        for (ApiTask task : tasks) {
            enqueue(task.getTaskId());
        }
    }

    /**
     * 将任务 ID 推入待消费队列。
     *
     * @param taskId 任务 ID
     */
    @Override
    public void enqueue(String taskId) {
        if (StringUtils.hasText(taskId)) {
            taskQueueBridge.offer(taskId);
        }
    }

    /**
     * Redis 任务队列桥接器。
     */
    @Service
    @RequiredArgsConstructor
    static class TaskQueueBridge {

        /**
         * Redis 字符串模板。
         */
        private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

        /**
         * 集成平台配置。
         */
        private final IntegrationProperties properties;

        /**
         * 入队任务 ID。
         *
         * @param taskId 任务 ID
         */
        void offer(String taskId) {
            stringRedisTemplate.opsForList().rightPush(properties.getQueue().getRedisKey(), taskId);
        }

        /**
         * 弹出任务 ID。
         *
         * @return 任务 ID
         */
        String poll() {
            return stringRedisTemplate.opsForList().leftPop(properties.getQueue().getRedisKey());
        }
    }
}

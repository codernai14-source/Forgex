package com.forgex.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.integration.config.IntegrationProperties;
import com.forgex.integration.domain.entity.ApiTask;
import com.forgex.integration.domain.entity.ApiTaskResult;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.domain.model.ApiTaskSubmitResult;
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
 * 寮傛浠诲姟鏈嶅姟
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiTaskServiceImpl extends ServiceImpl<ApiTaskMapper, ApiTask> implements IApiTaskService {

    private final ObjectMapper objectMapper;
    private final IApiTaskResultService apiTaskResultService;
    private final IntegrationProperties properties;
    private final TaskQueueBridge taskQueueBridge;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiTaskSubmitResult submit(ApiDefinitionSnapshot snapshot,
                                      ApiExecutionContext context,
                                      Map<String, Object> rawPayload,
                                      Map<String, Object> assembledPayload) {
        try {
            String taskId = UUID.randomUUID().toString().replace("-", "");
            ApiTask task = new ApiTask();
            task.setTaskId(taskId);
            task.setTraceId(context.getTraceId());
            task.setApiConfigId(snapshot.getApiConfig().getId());
            task.setApiCode(snapshot.getApiConfig().getApiCode());
            task.setDirection(snapshot.getApiConfig().getDirection());
            task.setProcessorBean(snapshot.getApiConfig().getProcessorBean());
            task.setInvokeMode(snapshot.getApiConfig().getInvokeMode());
            task.setRequestPayload(objectMapper.writeValueAsString(rawPayload));
            task.setAssembledPayload(objectMapper.writeValueAsString(assembledPayload));
            task.setStatus(ApiTaskStatusEnum.WAITING.name());
            task.setRetryCount(0);
            task.setMaxRetryCount(snapshot.getApiConfig().getRetryCount() == null ? 0 : snapshot.getApiConfig().getRetryCount());
            task.setNextExecuteTime(LocalDateTime.now());
            task.setTenantId(context.getTenantId());
            this.save(task);
            enqueue(taskId);
            return ApiTaskSubmitResult.builder()
                .taskId(taskId)
                .traceId(context.getTraceId())
                .status(ApiTaskStatusEnum.WAITING.name())
                .build();
        } catch (Exception ex) {
            throw new IllegalStateException("submit api task failed", ex);
        }
    }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSuccess(ApiTask task, Object result, int costTimeMs) {
        ApiTaskResult taskResult = new ApiTaskResult();
        taskResult.setTaskId(task.getTaskId());
        taskResult.setTraceId(task.getTraceId());
        taskResult.setApiConfigId(task.getApiConfigId());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailure(ApiTask task, String resultType, String errorMessage, int costTimeMs) {
        boolean canRetry = task.getRetryCount() != null
            && task.getMaxRetryCount() != null
            && task.getRetryCount() < task.getMaxRetryCount();
        if (canRetry) {
            task.setRetryCount(task.getRetryCount() + 1);
            task.setStatus(ApiTaskStatusEnum.RETRY.name());
            task.setNextExecuteTime(LocalDateTime.now().plusNanos(
                (task.getRetryCount() == null ? 1000 : Math.max(1000, task.getRetryCount() * 1000L)) * 1_000_000L));
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

    @Override
    public void enqueue(String taskId) {
        if (StringUtils.hasText(taskId)) {
            taskQueueBridge.offer(taskId);
        }
    }

    /**
     * Redis 闃熷垪妗ヨ繛鍣?
     */
    @Service
    @RequiredArgsConstructor
    static class TaskQueueBridge {

        private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;
        private final IntegrationProperties properties;

        void offer(String taskId) {
            stringRedisTemplate.opsForList().rightPush(properties.getQueue().getRedisKey(), taskId);
        }

        String poll() {
            return stringRedisTemplate.opsForList().leftPop(properties.getQueue().getRedisKey());
        }
    }
}

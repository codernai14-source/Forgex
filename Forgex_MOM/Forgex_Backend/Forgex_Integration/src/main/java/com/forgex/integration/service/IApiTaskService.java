package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.entity.ApiTask;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.domain.model.ApiTaskSubmitResult;

import java.util.Map;
import java.util.Optional;

public interface IApiTaskService extends IService<ApiTask> {

    ApiTaskSubmitResult submit(ApiDefinitionSnapshot snapshot,
                               ApiExecutionContext context,
                               Map<String, Object> rawPayload,
                               Map<String, Object> assembledPayload);

    Optional<ApiTask> claim(String taskId);

    void markSuccess(ApiTask task, Object result, int costTimeMs);

    void markFailure(ApiTask task, String resultType, String errorMessage, int costTimeMs);

    void requeueExpiredTasks();

    void enqueue(String taskId);
}

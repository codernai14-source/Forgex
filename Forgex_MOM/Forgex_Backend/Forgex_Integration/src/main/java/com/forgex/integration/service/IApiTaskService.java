package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.entity.ApiTask;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.domain.model.ApiTaskSubmitResult;
import java.util.Map;
import java.util.Optional;

/**
 * 接口任务服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IApiTaskService extends IService<ApiTask> {

    ApiTaskSubmitResult submit(ApiDefinitionSnapshot snapshot,
                               ApiExecutionContext context,
                               Map<String, Object> rawPayload,
                               Object assembledPayload);

    /**
     * 执行接口任务的claim操作。
     *
     * @param taskId 任务 ID
     * @return 处理结果
     */
    Optional<ApiTask> claim(String taskId);

    /**
     * 执行接口任务的marksuccess操作。
     *
     * @param task 任务
     * @param result 结果
     * @param costTimeMs 耗时时间ms
     */
    void markSuccess(ApiTask task, Object result, int costTimeMs);

    /**
     * 执行接口任务的markfailure操作。
     *
     * @param task 任务
     * @param resultType 结果类型
     * @param errorMessage 错误消息
     * @param costTimeMs 耗时时间ms
     */
    void markFailure(ApiTask task, String resultType, String errorMessage, int costTimeMs);

    /**
     * 执行接口任务的requeueexpiredtasks操作。
     */
    void requeueExpiredTasks();

    /**
     * 执行接口任务的enqueue操作。
     *
     * @param taskId 任务 ID
     */
    void enqueue(String taskId);
}

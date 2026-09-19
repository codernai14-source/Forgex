package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.dto.ApiTaskResultDTO;
import com.forgex.integration.domain.entity.ApiTaskResult;

/**
 * 接口任务结果服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IApiTaskResultService extends IService<ApiTaskResult> {

    /**
     * 获取by任务ID。
     *
     * @param taskId 任务 ID
     * @return 处理结果
     */
    ApiTaskResultDTO getByTaskId(String taskId);
}

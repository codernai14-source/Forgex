package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.dto.ApiTaskResultDTO;
import com.forgex.integration.domain.entity.ApiTaskResult;

public interface IApiTaskResultService extends IService<ApiTaskResult> {

    ApiTaskResultDTO getByTaskId(String taskId);
}

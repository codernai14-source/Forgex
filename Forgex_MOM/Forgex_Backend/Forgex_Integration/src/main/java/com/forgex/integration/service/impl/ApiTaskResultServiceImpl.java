package com.forgex.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.integration.domain.dto.ApiTaskResultDTO;
import com.forgex.integration.domain.entity.ApiTaskResult;
import com.forgex.integration.mapper.ApiTaskResultMapper;
import com.forgex.integration.service.IApiTaskResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiTaskResultServiceImpl extends ServiceImpl<ApiTaskResultMapper, ApiTaskResult>
    implements IApiTaskResultService {

    @Override
    public ApiTaskResultDTO getByTaskId(String taskId) {
        LambdaQueryWrapper<ApiTaskResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiTaskResult::getTaskId, taskId);
        wrapper.eq(ApiTaskResult::getDeleted, false);
        wrapper.last("LIMIT 1");
        ApiTaskResult entity = this.getOne(wrapper);
        if (entity == null) {
            return null;
        }
        ApiTaskResultDTO dto = new ApiTaskResultDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}

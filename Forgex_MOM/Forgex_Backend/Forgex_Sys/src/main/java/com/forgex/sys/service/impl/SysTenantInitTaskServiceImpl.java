/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.sys.domain.entity.SysTenantInitTask;
import com.forgex.sys.mapper.SysTenantInitTaskMapper;
import com.forgex.sys.service.SsePushService;
import com.forgex.sys.service.SysTenantInitTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

/**
 * 租户初始化任务服务实现类
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTenantInitTaskServiceImpl implements SysTenantInitTaskService {
    
    private final SysTenantInitTaskMapper tenantInitTaskMapper;
    private final SsePushService ssePushService;
    
    @Override
    public SysTenantInitTask getById(Long taskId) {
        return tenantInitTaskMapper.selectById(taskId);
    }
    
    @Override
    public SseEmitter subscribeProgress(Long taskId) {
        log.info("订阅租户初始化任务进度：{}", taskId);
        return ssePushService.createConnection(taskId.toString());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long taskId, Integer progress, String currentStep) {
        SysTenantInitTask task = tenantInitTaskMapper.selectById(taskId);
        if (task != null) {
            task.setProgress(progress);
            task.setCurrentStep(currentStep);
            tenantInitTaskMapper.updateById(task);
            
            // 推送进度到前端
            ssePushService.pushProgress(taskId.toString(), progress, currentStep);
            
            log.debug("更新任务进度，taskId: {}, progress: {}, currentStep: {}", taskId, progress, currentStep);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSuccess(Long taskId) {
        SysTenantInitTask task = tenantInitTaskMapper.selectById(taskId);
        if (task != null) {
            task.setStatus("SUCCESS");
            task.setProgress(100);
            task.setCurrentStep("初始化完成");
            task.setEndTime(LocalDateTime.now());
            tenantInitTaskMapper.updateById(task);
            
            // 推送完成消息
            ssePushService.pushComplete(taskId.toString(), true, "租户初始化成功");
            
            log.info("租户初始化任务成功，taskId: {}", taskId);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailed(Long taskId, String errorMessage) {
        SysTenantInitTask task = tenantInitTaskMapper.selectById(taskId);
        if (task != null) {
            task.setStatus("FAILED");
            task.setErrorMessage(errorMessage);
            task.setEndTime(LocalDateTime.now());
            tenantInitTaskMapper.updateById(task);
            
            // 推送失败消息
            ssePushService.pushComplete(taskId.toString(), false, errorMessage);
            
            log.error("租户初始化任务失败，taskId: {}, error: {}", taskId, errorMessage);
        }
    }
    
    @Override
    public SysTenantInitTask getByTenantId(Long tenantId) {
        LambdaQueryWrapper<SysTenantInitTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenantInitTask::getTenantId, tenantId)
               .orderByDesc(SysTenantInitTask::getCreateTime)
               .last("limit 1");
        
        SysTenantInitTask task = tenantInitTaskMapper.selectOne(wrapper);
        log.debug("根据租户 ID 查询任务，tenantId: {}, taskId: {}", tenantId, task != null ? task.getId() : null);
        return task;
    }
}

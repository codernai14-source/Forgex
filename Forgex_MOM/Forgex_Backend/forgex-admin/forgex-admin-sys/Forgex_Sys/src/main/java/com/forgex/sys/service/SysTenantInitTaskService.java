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
package com.forgex.sys.service;

import com.forgex.sys.domain.entity.SysTenantInitTask;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 租户初始化任务服务接口
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface SysTenantInitTaskService {
    
    /**
     * 根据 ID 获取任务
     *
     * @param taskId 任务 ID
     * @return 任务实体
     */
    SysTenantInitTask getById(Long taskId);
    
    /**
     * 订阅任务进度（SSE）
     *
     * @param taskId 任务 ID
     * @return SSE 发射器
     */
    SseEmitter subscribeProgress(Long taskId);
    
    /**
     * 更新任务进度
     *
     * @param taskId 任务 ID
     * @param progress 进度百分比
     * @param currentStep 当前步骤
     */
    void updateProgress(Long taskId, Integer progress, String currentStep);
    
    /**
     * 标记任务成功
     *
     * @param taskId 任务 ID
     */
    void markSuccess(Long taskId);
    
    /**
     * 标记任务失败
     *
     * @param taskId 任务 ID
     * @param errorMessage 错误信息
     */
    void markFailed(Long taskId, String errorMessage);
    
    /**
     * 根据租户 ID 获取任务
     *
     * @param tenantId 租户 ID
     * @return 任务实体，不存在时返回 null
     */
    SysTenantInitTask getByTenantId(Long tenantId);
}

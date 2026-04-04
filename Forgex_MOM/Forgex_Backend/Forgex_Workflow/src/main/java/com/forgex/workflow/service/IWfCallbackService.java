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
package com.forgex.workflow.service;

/**
 * 审批回调服务接口。
 * <p>
 * 提供审批回调的注册和触发功能。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
public interface IWfCallbackService {
    
    /**
     * 注册回调
     * 
     * @param taskCode 任务编码
     * @param callbackUrl 回调 URL
     * @param callbackBean 回调 Bean 名称
     */
    void registerCallback(String taskCode, String callbackUrl, String callbackBean);
    
    /**
     * 注销回调
     * 
     * @param taskCode 任务编码
     */
    void unregisterCallback(String taskCode);
    
    /**
     * 触发回调
     * 
     * @param executionId 执行 ID
     * @param status 最终状态
     */
    void triggerCallback(Long executionId, Integer status);
}

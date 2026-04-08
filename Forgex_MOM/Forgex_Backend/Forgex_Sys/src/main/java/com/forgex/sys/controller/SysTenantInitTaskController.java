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
package com.forgex.sys.controller;

import com.forgex.common.web.R;
import com.forgex.sys.domain.entity.SysTenantInitTask;
import com.forgex.sys.service.SysTenantInitTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 租户初始化任务控制器
 * <p>
 * 提供租户初始化任务的查询和进度订阅接口。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/tenant/init/task")
@RequiredArgsConstructor
public class SysTenantInitTaskController {
    
    private final SysTenantInitTaskService tenantInitTaskService;
    
    /**
     * 获取任务详情
     *
     * @param taskId 任务 ID
     * @return 任务详情
     */
    @GetMapping("/detail/{taskId}")
    public R<SysTenantInitTask> getTaskDetail(@PathVariable Long taskId) {
        SysTenantInitTask task = tenantInitTaskService.getById(taskId);
        return R.ok(task);
    }
    
    /**
     * 订阅进度推送（SSE）
     * <p>
     * 客户端通过此接口建立 SSE 长连接，实时接收任务进度更新。
     * </p>
     *
     * @param taskId 任务 ID
     * @return SSE 发射器
     */
    @GetMapping(value = "/progress/{taskId}", produces = "text/event-stream")
    public SseEmitter subscribeProgress(@PathVariable Long taskId) {
        return tenantInitTaskService.subscribeProgress(taskId);
    }
    
    /**
     * 根据租户 ID 获取任务
     *
     * @param tenantId 租户 ID
     * @return 任务详情
     */
    @GetMapping("/by-tenant/{tenantId}")
    public R<SysTenantInitTask> getTaskByTenantId(@PathVariable Long tenantId) {
        SysTenantInitTask task = tenantInitTaskService.getByTenantId(tenantId);
        return R.ok(task);
    }
}

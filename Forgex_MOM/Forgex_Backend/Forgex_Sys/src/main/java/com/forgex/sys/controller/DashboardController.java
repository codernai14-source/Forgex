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

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.sys.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘 Controller
 * 
 * @author coder_nai@163.com
 * @date 2025-01-08
 * @version 1.1
 */
@RestController
@RequestMapping("/sys/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     *
     * @param body 请求体参数
     * @return 统计数据
     */
    @PostMapping("/statistics")
    public R<Map<String, Object>> getStatistics(@RequestBody Map<String, Object> body) {
        Long tenantId = TenantContext.get();

        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }

        Map<String, Object> statistics = dashboardService.getStatistics(tenantId);

        return R.ok(statistics);
    }

    /**
     * 获取服务器信息
     *
     * @return 服务器信息
     */
    @GetMapping("/serverInfo")
    public R<Map<String, Object>> getServerInfo() {
        Map<String, Object> serverInfo = dashboardService.getServerInfo();
        return R.ok(serverInfo);
    }

    /**
     * 获取模块使用情况
     *
     * @return 模块使用数据列表
     */
    @GetMapping("/moduleUsage")
    public R<List<Map<String, Object>>> getModuleUsage() {
        List<Map<String, Object>> moduleUsage = dashboardService.getModuleUsage();
        return R.ok(moduleUsage);
    }

    /**
     * 获取 JVM 各内存分区占用（柱状图）
     *
     * @return 内存池列表
     */
    @GetMapping("/moduleMemoryUsage")
    public R<List<Map<String, Object>>> getModuleMemoryUsage() {
        return R.ok(dashboardService.getModuleMemoryUsage());
    }

    /**
     * 获取服务内存使用情况
     *
     * @return 服务内存使用数据列表
     */
    @GetMapping("/serviceMemoryUsage")
    public R<List<Map<String, Object>>> getServiceMemoryUsage() {
        List<Map<String, Object>> memoryUsage = dashboardService.getServiceMemoryUsage();
        return R.ok(memoryUsage);
    }

    /**
     * 获取最近操作日志
     *
     * @param size 数量，默认 5 条
     * @return 操作日志列表
     */
    @GetMapping("/recentOperationLogs")
    public R<List<Map<String, Object>>> getRecentOperationLogs(
            @RequestParam(value = "size", defaultValue = "5") Integer size) {
        List<Map<String, Object>> logs = dashboardService.getRecentOperationLogs(size);
        return R.ok(logs);
    }

    /**
     * 获取最近登录日志
     *
     * @param size 数量，默认 5 条
     * @return 登录日志列表
     */
    @GetMapping("/recentLoginLogs")
    public R<List<Map<String, Object>>> getRecentLoginLogs(
            @RequestParam(value = "size", defaultValue = "5") Integer size) {
        List<Map<String, Object>> logs = dashboardService.getRecentLoginLogs(size);
        return R.ok(logs);
    }

    /**
     * 解析 Long 类型参数
     */
    private Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}

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
import com.forgex.sys.domain.entity.LoginLog;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.entity.SysOperationLog;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.mapper.LoginLogMapper;
import com.forgex.sys.mapper.SysMenuMapper;
import com.forgex.sys.mapper.SysOperationLogMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.service.IDashboardService;
import com.forgex.sys.service.IOnlineUserService;
import com.sun.management.OperatingSystemMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘 Service 实现类
 * <p>
 * 提供租户统计、服务器资源（CPU/内存）、模块访问、最近日志等数据。
 * CPU 使用率优先使用 {@link OperatingSystemMXBean#getSystemCpuLoad()}（整机占用，与 Windows 任务管理器「CPU」列总和趋势一致），
 * 不可用时再退回 {@link OperatingSystemMXBean#getProcessCpuLoad()}（仅 JVM 进程，空闲时常接近 0）。
 * </p>
 *
 * @author coder_nai@163.com
 * @date 2025-01-08
 * @version 1.2
 * @see IDashboardService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysOperationLogMapper operationLogMapper;
    private final LoginLogMapper loginLogMapper;
    private final IOnlineUserService onlineUserService;

    /**
     * 产品版本号（与前端展示「系统版本」对应，来自配置 forgex.application.version）
     */
    @Value("${forgex.application.version:1.0.0}")
    private String applicationVersion;

    /**
     * 仪表盘地图标注经度
     */
    @Value("${forgex.dashboard.map-longitude:116.407526}")
    private double mapLongitude;

    /**
     * 仪表盘地图标注纬度
     */
    @Value("${forgex.dashboard.map-latitude:39.90403}")
    private double mapLatitude;

    /**
     * 仪表盘地图地点名称
     */
    @Value("${forgex.dashboard.map-location-name:北京市}")
    private String mapLocationName;

    @Override
    public Map<String, Object> getStatistics(Long tenantId) {
        Map<String, Object> result = new HashMap<>();

        // 按当前租户统计（避免把全库用户算进「用户总数」）
        Long userCount = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getDeleted, false)
        );

        Long roleCount = roleMapper.selectCount(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false)
        );

        Long menuCount = menuMapper.selectCount(
            new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getTenantId, tenantId)
                .eq(SysMenu::getDeleted, false)
        );

        Long onlineUsers = onlineUserService.countOnlineUsers(tenantId);

        result.put("userCount", userCount);
        result.put("roleCount", roleCount);
        result.put("menuCount", menuCount);
        result.put("onlineUsers", onlineUsers);

        return result;
    }

    @Override
    public Map<String, Object> getServerInfo() {
        Map<String, Object> result = new HashMap<>();

        try {
            java.lang.management.OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

            result.put("osName", System.getProperty("os.name"));
            // 操作系统内核/发行版版本（与「应用版本」区分）
            result.put("osKernelVersion", System.getProperty("os.version"));
            result.put("osArch", osBean.getArch());

            // 本系统（Forgex）产品版本
            result.put("appVersion", applicationVersion);

            // 地图标注（供前端中国地图展示）
            result.put("mapLongitude", mapLongitude);
            result.put("mapLatitude", mapLatitude);
            result.put("mapLocationName", mapLocationName);

            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heap = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();

            long heapUsed = heap.getUsed();
            long heapMax = heap.getMax() > 0 ? heap.getMax() : heap.getCommitted();
            long heapFree = Math.max(0L, heapMax - heapUsed);
            long nonHeapUsed = nonHeap.getUsed();

            // JVM 视角内存（单位 GB，保留 2 位小数）
            result.put("jvmHeapUsedGb", round2Gb(heapUsed));
            result.put("jvmHeapFreeGb", round2Gb(heapFree));
            result.put("jvmNonHeapUsedGb", round2Gb(nonHeapUsed));
            result.put("jvmHeapMaxGb", round2Gb(heapMax));

            // 机器物理内存（用户关心的「32G」等）：总/已用/可用
            boolean physicalOk = false;
            if (osBean instanceof OperatingSystemMXBean) {
                OperatingSystemMXBean sunOs = (OperatingSystemMXBean) osBean;
                long totalPhys = sunOs.getTotalPhysicalMemorySize();
                long freePhys = sunOs.getFreePhysicalMemorySize();
                if (totalPhys > 0) {
                    physicalOk = true;
                    result.put("physicalTotalGb", round2Gb(totalPhys));
                    result.put("physicalFreeGb", round2Gb(freePhys));
                    result.put("physicalUsedGb", round2Gb(totalPhys - freePhys));
                    result.put("totalMemory", round2Gb(totalPhys));
                    result.put("usedMemory", round2Gb(totalPhys - freePhys));
                    result.put("availableMemory", round2Gb(freePhys));
                }
            }
            if (!physicalOk) {
                // 无法读取物理内存时退回 JVM 堆上限说明，避免空白
                result.put("totalMemory", round2Gb(heapMax));
                result.put("usedMemory", round2Gb(heapUsed + nonHeapUsed));
                result.put("availableMemory", round2Gb(heapFree));
            }

            result.put("javaVersion", System.getProperty("java.version"));
            String runtimeName = System.getProperty("java.runtime.name");
            if (runtimeName == null || runtimeName.isEmpty()) {
                runtimeName = System.getProperty("java.vm.name");
            }
            result.put("jvmName", runtimeName + " " + System.getProperty("java.version"));

            result.put("cpuCores", osBean.getAvailableProcessors());
            result.put("cpuModel", resolveCpuModel(osBean));

            result.put("cpuUsage", resolveCpuUsagePercent(osBean));

        } catch (Exception e) {
            log.error("获取服务器信息失败", e);
            result.put("error", "获取服务器信息失败：" + e.getMessage());
            result.put("cpuUsage", 0.0);
        }

        return result;
    }

    /**
     * 将字节数转换为 GB，保留两位小数
     *
     * @param bytes 字节
     * @return GB
     */
    private static double round2Gb(long bytes) {
        return Math.round(bytes / 1024.0 / 1024.0 / 1024.0 * 100.0) / 100.0;
    }

    /**
     * 解析 CPU 型号说明（可读文本，避免仅显示 64-bit）
     *
     * @param osBean 系统 MXBean
     * @return 展示用 CPU 描述
     */
    private static String resolveCpuModel(java.lang.management.OperatingSystemMXBean osBean) {
        String env = System.getenv("PROCESSOR_IDENTIFIER");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return osBean.getArch() + " · " + System.getProperty("os.arch", "");
    }

    /**
     * 计算 CPU 占用百分比（0~100），用于仪表盘展示。
     * <p>
     * 优先取整机 CPU（{@link OperatingSystemMXBean#getSystemCpuLoad()}），与用户习惯的「任务管理器总占用」一致；
     * 若返回不可用（-1），再取 JVM 进程 CPU（{@link OperatingSystemMXBean#getProcessCpuLoad()}）。
     * Windows 上 {@link java.lang.management.OperatingSystemMXBean#getSystemLoadAverage()} 常为 -1，故不使用为首选。
     * </p>
     *
     * @param osBean 标准 OperatingSystemMXBean
     * @return 0~100 的 CPU 使用率（百分比数值，非小数比）
     */
    private static double resolveCpuUsagePercent(java.lang.management.OperatingSystemMXBean osBean) {
        if (osBean instanceof OperatingSystemMXBean) {
            OperatingSystemMXBean sun = (OperatingSystemMXBean) osBean;
            // 整机 CPU：与任务管理器「CPU」总占用更接近
            double s = sun.getSystemCpuLoad();
            if (s >= 0) {
                return Math.min(100.0, Math.max(0.0, s * 100.0));
            }
            double p = sun.getProcessCpuLoad();
            if (p >= 0) {
                return Math.min(100.0, Math.max(0.0, p * 100.0));
            }
        }
        double loadAvg = osBean.getSystemLoadAverage();
        if (loadAvg >= 0) {
            int cores = Math.max(1, osBean.getAvailableProcessors());
            return Math.min(100.0, Math.max(0.0, (loadAvg / cores) * 100.0));
        }
        return 0.0;
    }

    @Override
    public List<Map<String, Object>> getModuleUsage() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(SysOperationLog::getModule);
            queryWrapper.orderByDesc(SysOperationLog::getOperationTime);
            queryWrapper.last("LIMIT 500");

            List<SysOperationLog> logs = operationLogMapper.selectList(queryWrapper);

            Map<String, Long> moduleCount = new HashMap<>();
            for (SysOperationLog row : logs) {
                String module = row.getModule();
                if (module != null && !module.isEmpty()) {
                    moduleCount.put(module, moduleCount.getOrDefault(module, 0L) + 1);
                }
            }

            moduleCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("moduleName", entry.getKey());
                    item.put("operationCount", entry.getValue());
                    result.add(item);
                });

        } catch (Exception e) {
            log.error("获取模块使用情况失败", e);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getServiceMemoryUsage() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heap = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();

            long heapUsed = heap.getUsed();
            long heapMax = heap.getMax() > 0 ? heap.getMax() : heap.getCommitted();
            long heapFree = Math.max(0L, heapMax - heapUsed);
            long nonHeapUsed = nonHeap.getUsed();

            // 饼图：三块均为 JVM 真实数据，单位 MB，名称与前端 legend 一致
            Map<String, Object> heapUsedItem = new HashMap<>();
            heapUsedItem.put("serviceName", "堆内存(已用)");
            heapUsedItem.put("memoryUsage", heapUsed / 1024 / 1024);
            result.add(heapUsedItem);

            Map<String, Object> nonHeapItem = new HashMap<>();
            nonHeapItem.put("serviceName", "非堆内存(已用)");
            nonHeapItem.put("memoryUsage", nonHeapUsed / 1024 / 1024);
            result.add(nonHeapItem);

            Map<String, Object> heapFreeItem = new HashMap<>();
            heapFreeItem.put("serviceName", "堆内存(空闲)");
            heapFreeItem.put("memoryUsage", heapFree / 1024 / 1024);
            result.add(heapFreeItem);

        } catch (Exception e) {
            log.error("获取服务内存使用情况失败", e);
        }

        return result;
    }

    /**
     * JVM 各内存池（分区）已用内存，用于柱状图展示。
     * <p>业务上常称为「内存分区」，与操作系统进程级模块占用不同，为 JVM 可观测的真实数据。</p>
     *
     * @return 每项包含 moduleName（池名称）、memoryUsageMb（已用 MB）
     */
    @Override
    public List<Map<String, Object>> getModuleMemoryUsage() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                MemoryUsage u = pool.getUsage();
                if (u == null) {
                    continue;
                }
                long used = u.getUsed();
                Map<String, Object> row = new HashMap<>(4);
                row.put("moduleName", pool.getName());
                row.put("memoryUsageMb", used / 1024 / 1024);
                result.add(row);
            }
            result.sort((a, b) -> Long.compare(
                ((Number) b.get("memoryUsageMb")).longValue(),
                ((Number) a.get("memoryUsageMb")).longValue()));
        } catch (Exception e) {
            log.error("获取 JVM 内存分区占用失败", e);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentOperationLogs(Integer size) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(SysOperationLog::getOperationTime);
            queryWrapper.last("LIMIT " + (size != null ? size : 5));

            List<SysOperationLog> logs = operationLogMapper.selectList(queryWrapper);

            for (SysOperationLog row : logs) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", row.getId());
                item.put("operationTime", row.getOperationTime());
                item.put("operatorName", row.getUsername());
                item.put("operationModule", row.getModule());
                String desc = row.getDetailText();
                if (desc == null || desc.isBlank()) {
                    desc = row.getOperationType();
                }
                if (desc == null || desc.isBlank()) {
                    desc = row.getRequestUrl();
                }
                item.put("operationDescription", desc != null ? desc : "");
                item.put("status", row.getResponseStatus() != null && row.getResponseStatus() == 200 ? 0 : 1);
                result.add(item);
            }

        } catch (Exception e) {
            log.error("获取最近操作日志失败", e);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentLoginLogs(Integer size) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            LambdaQueryWrapper<LoginLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(LoginLog::getLoginTime);
            queryWrapper.last("LIMIT " + (size != null ? size : 5));

            List<LoginLog> logs = loginLogMapper.selectList(queryWrapper);

            for (LoginLog row : logs) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", row.getId());
                item.put("loginTime", row.getLoginTime());
                item.put("username", row.getAccount());
                item.put("ipAddress", row.getLoginIp());
                item.put("loginLocation", row.getLoginRegion());
                item.put("status", row.getStatus() != null && row.getStatus() == 1 ? 0 : 1);
                result.add(item);
            }

        } catch (Exception e) {
            log.error("获取最近登录日志失败", e);
        }

        return result;
    }
}

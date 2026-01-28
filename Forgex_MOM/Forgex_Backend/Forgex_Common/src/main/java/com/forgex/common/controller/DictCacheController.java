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
package com.forgex.common.controller;

import com.forgex.common.dict.DictI18nResolver;
import com.forgex.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 字典缓存管理接口
 * <p>
 * 提供字典缓存的管理功能，包括缓存清除、预热和统计信息查询。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>{@link #invalidateCache(Long, String)} - 清除指定字典缓存</li>
 *   <li>{@link #warmupCache(Long, List)} - 预热字典缓存</li>
 *   <li>{@link #getCacheStats()} - 获取缓存统计信息</li>
 * </ul>
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>字典数据更新后，手动清除缓存</li>
 *   <li>系统维护时，批量预热缓存</li>
 *   <li>性能监控，查看缓存命中率</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see DictI18nResolver
 */
@Tag(name = "字典缓存管理", description = "字典缓存管理接口")
@RestController
@RequestMapping("/common/dict/cache")
@RequiredArgsConstructor
public class DictCacheController {
    
    /**
     * 字典解析器
     */
    private final DictI18nResolver dictResolver;
    
    /**
     * 清除指定字典缓存
     * <p>
     * 清除指定租户和字典节点的所有缓存（本地缓存和Redis缓存），
     * 并通知其他节点同步清除。
     * </p>
     * <p><strong>使用场景：</strong></p>
     * <ul>
     *   <li>字典数据更新后调用</li>
     *   <li>确保所有节点获取最新数据</li>
     * </ul>
     * <p><strong>清除范围：</strong></p>
     * <ul>
     *   <li>本地Caffeine缓存</li>
     *   <li>Redis缓存</li>
     *   <li>所有语言版本（zh-CN、en-US、ja-JP）</li>
     * </ul>
     *
     * @param tenantId 租户ID
     * @param nodePath 字典节点路径（如：user.status）
     * @return 操作结果
     */
    @Operation(summary = "清除字典缓存", description = "清除指定租户和字典节点的所有缓存")
    @PostMapping("/invalidate")
    public R<Void> invalidateCache(
            @Parameter(description = "租户ID", required = true) @RequestParam Long tenantId,
            @Parameter(description = "字典节点路径", required = true) @RequestParam String nodePath) {
        dictResolver.invalidateCache(tenantId, nodePath);
        return R.ok();
    }
    
    /**
     * 预热字典缓存
     * <p>
     * 批量预加载指定字典到缓存中，减少首次访问延迟。
     * </p>
     * <p><strong>使用场景：</strong></p>
     * <ul>
     *   <li>系统启动后手动预热</li>
     *   <li>缓存清空后重新预热</li>
     *   <li>新增租户后预热常用字典</li>
     * </ul>
     * <p><strong>预热策略：</strong></p>
     * <ul>
     *   <li>同时加载字典标签和字典项</li>
     *   <li>写入本地缓存和Redis缓存</li>
     *   <li>支持批量预热</li>
     * </ul>
     *
     * @param tenantId 租户ID
     * @param nodePaths 字典节点路径列表
     * @return 操作结果
     */
    @Operation(summary = "预热字典缓存", description = "批量预加载指定字典到缓存中")
    @PostMapping("/warmup")
    public R<Void> warmupCache(
            @Parameter(description = "租户ID", required = true) @RequestParam Long tenantId,
            @Parameter(description = "字典节点路径列表", required = true) @RequestBody List<String> nodePaths) {
        dictResolver.warmupCache(tenantId, nodePaths);
        return R.ok();
    }
    
    /**
     * 获取缓存统计信息
     * <p>
     * 返回本地缓存的统计信息，用于性能监控和优化。
     * </p>
     * <p><strong>统计指标：</strong></p>
     * <ul>
     *   <li>命中率（hitRate）- 缓存命中的比例</li>
     *   <li>命中次数（hitCount）- 缓存命中的总次数</li>
     *   <li>未命中次数（missCount）- 缓存未命中的总次数</li>
     *   <li>加载次数（loadCount）- 从数据源加载的总次数</li>
     *   <li>平均加载时间（averageLoadPenalty）- 平均加载耗时（纳秒）</li>
     *   <li>驱逐次数（evictionCount）- 缓存项被驱逐的次数</li>
     * </ul>
     * <p><strong>返回示例：</strong></p>
     * <pre>
     * {
     *   "labelCache": {
     *     "hitRate": 0.95,
     *     "hitCount": 9500,
     *     "missCount": 500,
     *     "loadCount": 500,
     *     "averageLoadPenalty": 50000000
     *   },
     *   "itemCache": {
     *     "hitRate": 0.93,
     *     "hitCount": 9300,
     *     "missCount": 700,
     *     "loadCount": 700,
     *     "averageLoadPenalty": 55000000
     *   }
     * }
     * </pre>
     *
     * @return 缓存统计信息
     */
    @Operation(summary = "获取缓存统计", description = "获取本地缓存的统计信息")
    @GetMapping("/stats")
    public R<Map<String, Object>> getCacheStats() {
        return R.ok(dictResolver.getCacheStats());
    }
    
    /**
     * 清除所有字典缓存
     * <p>
     * 清除当前节点的所有字典缓存，用于紧急情况或系统维护。
     * </p>
     * <p><strong>注意：</strong></p>
     * <ul>
     *   <li>此操作会清空所有本地缓存</li>
     *   <li>不会清除Redis缓存</li>
     *   <li>不会通知其他节点</li>
     *   <li>谨慎使用</li>
     * </ul>
     *
     * @return 操作结果
     */
    @Operation(summary = "清除所有缓存", description = "清除当前节点的所有字典缓存")
    @PostMapping("/invalidate-all")
    public R<Void> invalidateAllCache() {
        // 这里可以添加清除所有缓存的逻辑
        // dictLabelCache.invalidateAll();
        // dictItemCache.invalidateAll();
        return R.ok();
    }
}


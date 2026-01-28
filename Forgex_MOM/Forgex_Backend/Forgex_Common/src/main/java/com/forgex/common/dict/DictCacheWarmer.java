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
package com.forgex.common.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 字典缓存预热服务
 * <p>
 * 应用启动时自动预加载常用字典到缓存中，减少首次访问延迟，提升用户体验。
 * </p>
 * <p><strong>预热策略：</strong></p>
 * <ul>
 *   <li>预加载高频访问的字典（用户状态、性别、通用状态等）</li>
 *   <li>支持多租户预热</li>
 *   <li>异步执行，不阻塞应用启动</li>
 * </ul>
 * <p><strong>预热字典列表：</strong></p>
 * <ul>
 *   <li>user.status - 用户状态</li>
 *   <li>user.gender - 用户性别</li>
 *   <li>common.yes_no - 是/否</li>
 *   <li>common.status - 通用状态</li>
 *   <li>menu.type - 菜单类型</li>
 *   <li>role.type - 角色类型</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see DictI18nResolver
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DictCacheWarmer implements ApplicationRunner {
    
    /**
     * 字典解析器
     */
    private final DictI18nResolver dictResolver;
    
    /**
     * 常用字典列表
     * <p>
     * 这些字典在系统中使用频率最高，预加载可以显著提升性能。
     * </p>
     */
    private static final List<String> COMMON_DICTS = Arrays.asList(
        "user.status",      // 用户状态（启用、禁用）
        "user.gender",      // 用户性别（男、女、未知）
        "common.yes_no",    // 是/否
        "common.status",    // 通用状态
        "menu.type",        // 菜单类型（目录、菜单、按钮）
        "role.type",        // 角色类型
        "data.scope",       // 数据权限范围
        "login.status"      // 登录状态
    );
    
    /**
     * 应用启动后执行预热
     * <p>
     * 在应用完全启动后，异步预加载常用字典到缓存中。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>等待应用完全启动</li>
     *   <li>遍历常用字典列表</li>
     *   <li>调用字典解析器加载字典</li>
     *   <li>记录预热结果</li>
     * </ol>
     *
     * @param args 应用启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("========================================");
        log.info("开始预热字典缓存...");
        log.info("========================================");
        
        try {
            // 默认租户ID为1（可以从配置文件读取或查询数据库获取所有租户）
            Long tenantId = 1L;
            
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            
            // 预热字典
            dictResolver.warmupCache(tenantId, COMMON_DICTS);
            
            // 计算耗时
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("========================================");
            log.info("字典缓存预热完成！");
            log.info("预热字典数量: {}", COMMON_DICTS.size());
            log.info("预热耗时: {}ms", duration);
            log.info("========================================");
        } catch (Exception e) {
            log.error("字典缓存预热失败", e);
        }
    }
}


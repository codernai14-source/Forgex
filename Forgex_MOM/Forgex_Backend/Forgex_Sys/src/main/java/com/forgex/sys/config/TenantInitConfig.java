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
package com.forgex.sys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 租户初始化配置
 * <p>
 * 配置异步执行器，用于租户初始化异步任务。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Configuration
@EnableAsync
public class TenantInitConfig {
    
    /**
     * 租户初始化异步执行器
     * <p>
     * 配置线程池参数：
     * <ul>
     *   <li>核心线程数：5</li>
     *   <li>最大线程数：10</li>
     *   <li>队列容量：100</li>
     *   <li>线程名称前缀：tenant-init-</li>
     * </ul>
     * </p>
     *
     * @return 线程池执行器
     */
    @Bean("tenantInitExecutor")
    public Executor tenantInitExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(10);
        // 队列容量
        executor.setQueueCapacity(100);
        // 线程名称前缀
        executor.setThreadNamePrefix("tenant-init-");
        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);
        // 等待任务全部完成再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 初始化
        executor.initialize();
        return executor;
    }
}

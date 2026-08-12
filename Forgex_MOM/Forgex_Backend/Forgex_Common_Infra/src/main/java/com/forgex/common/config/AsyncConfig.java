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
package com.forgex.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类
 * <p>
 * 配置异步任务执行器，用于异步记录登录日志、发送通知等非阻塞操作。
 * </p>
 * <p><strong>配置说明：</strong></p>
 * <ul>
 *   <li>核心线程数: 5 - 保持活跃的线程数</li>
 *   <li>最大线程数: 10 - 高峰期最多创建的线程数</li>
 *   <li>队列容量: 100 - 等待执行的任务队列大小</li>
 *   <li>线程空闲时间: 60秒 - 超过核心线程数的线程空闲后的存活时间</li>
 *   <li>拒绝策略: CallerRunsPolicy - 队列满时由调用者线程执行</li>
 * </ul>
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>异步记录登录日志</li>
 *   <li>异步发送邮件/短信</li>
 *   <li>异步更新统计数据</li>
 *   <li>其他不阻塞主流程的操作</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see org.springframework.scheduling.annotation.Async
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * 异步任务执行器
     * <p>
     * 创建一个线程池用于执行异步任务，避免阻塞主线程。
     * </p>
     * <p><strong>线程池参数说明：</strong></p>
     * <ul>
     *   <li>corePoolSize: 核心线程数，即使空闲也会保持活跃</li>
     *   <li>maxPoolSize: 最大线程数，高峰期最多创建的线程数</li>
     *   <li>queueCapacity: 队列容量，等待执行的任务队列大小</li>
     *   <li>keepAliveSeconds: 线程空闲时间，超过核心线程数的线程空闲后的存活时间</li>
     *   <li>rejectedExecutionHandler: 拒绝策略，队列满时的处理方式</li>
     * </ul>
     * <p><strong>拒绝策略：</strong></p>
     * <ul>
     *   <li>CallerRunsPolicy: 由调用者线程执行任务，避免任务丢失</li>
     *   <li>优点: 不会丢失任务，提供降级处理</li>
     *   <li>缺点: 可能会阻塞调用者线程</li>
     * </ul>
     *
     * @return 线程池执行器
     */
    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        log.info("初始化异步任务执行器...");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：保持活跃的线程数
        executor.setCorePoolSize(5);
        
        // 最大线程数：高峰期最多创建的线程数
        executor.setMaxPoolSize(10);
        
        // 队列容量：等待执行的任务队列大小
        executor.setQueueCapacity(100);
        
        // 线程名称前缀：便于日志追踪
        executor.setThreadNamePrefix("async-task-");
        
        // 线程空闲时间（秒）：超过核心线程数的线程空闲后的存活时间
        executor.setKeepAliveSeconds(60);
        
        // 拒绝策略：队列满时由调用者线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务完成后关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）：关闭线程池时等待任务完成的最长时间
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化线程池
        executor.initialize();
        
        log.info("异步任务执行器初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}


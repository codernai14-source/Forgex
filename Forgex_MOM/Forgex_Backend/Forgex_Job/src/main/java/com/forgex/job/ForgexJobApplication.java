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
package com.forgex.job;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务服务启动类
 * <p>
 * 负责启动定时任务微服务，用于执行系统中的定时任务和异步任务
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>定时任务：支持基于 Cron 表达式的定时任务调度</li>
 *   <li>异步任务：支持异步执行的业务逻辑</li>
 *   <li>任务管理：提供任务的启动、停止、监控等功能</li>
 * </ul>
 * <p>使用场景：</p>
 * <ul>
 *   <li>数据清理：定期清理过期数据</li>
 *   <li>数据统计：定期统计和汇总数据</li>
 *   <li>消息推送：定时推送通知消息</li>
 *   <li>数据同步：定期同步外部数据</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see org.springframework.scheduling.annotation.Scheduled
 * @see org.springframework.scheduling.annotation.EnableAsync
 */
@SpringBootApplication(scanBasePackages = {"com.forgex.job", "com.forgex.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.forgex.common.feign.client")
@EnableAsync
@EnableScheduling
@MapperScan({"com.forgex.job.mapper", "com.forgex.common.mapper"})
public class ForgexJobApplication {
    /**
     * 应用入口
     * <p>启动 Spring Boot 应用</p>
     *
     * @param args 启动参数
     * @see org.springframework.boot.SpringApplication#run(Class, String[])
     */
    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(ForgexJobApplication.class, args);
    }
}

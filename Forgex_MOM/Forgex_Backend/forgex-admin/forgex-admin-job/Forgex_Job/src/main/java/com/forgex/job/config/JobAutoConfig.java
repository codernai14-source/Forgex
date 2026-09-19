package com.forgex.job.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Job 模块 Spring 配置。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Configuration
@EnableConfigurationProperties(JobProperties.class)
public class JobAutoConfig {

    @Bean("jobTaskExecutor")
    public Executor jobTaskExecutor(JobProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("forgex-job-");
        executor.setCorePoolSize(properties.getExecutor().getCorePoolSize());
        executor.setMaxPoolSize(properties.getExecutor().getMaxPoolSize());
        executor.setQueueCapacity(properties.getExecutor().getQueueCapacity());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(properties.getExecutor().getShutdownAwaitSeconds());
        executor.initialize();
        return executor;
    }
}

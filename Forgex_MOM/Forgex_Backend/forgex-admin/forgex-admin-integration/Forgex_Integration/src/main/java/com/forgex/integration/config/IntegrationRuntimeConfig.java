package com.forgex.integration.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Integration 运行时配置。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(IntegrationProperties.class)
public class IntegrationRuntimeConfig {

    /**
     * 同步执行线程池。
     *
     * @param properties 集成配置
     * @return 线程池执行器
     */
    @Bean("integrationSyncExecutor")
    public ThreadPoolTaskExecutor integrationSyncExecutor(IntegrationProperties properties) {
        return buildExecutor("integration-sync-", properties.getSyncExecutor());
    }

    /**
     * 异步消费线程池。
     *
     * @param properties 集成配置
     * @return 线程池执行器
     */
    @Bean("integrationAsyncConsumerExecutor")
    public ThreadPoolTaskExecutor integrationAsyncConsumerExecutor(IntegrationProperties properties) {
        return buildExecutor("integration-consumer-", properties.getAsyncConsumerExecutor());
    }

    /**
     * 日志写入线程池。
     *
     * @param properties 集成配置
     * @return 线程池执行器
     */
    @Bean("integrationLogExecutor")
    public ThreadPoolTaskExecutor integrationLogExecutor(IntegrationProperties properties) {
        return buildExecutor("integration-log-", properties.getLogExecutor());
    }

    /**
     * 集成接口调用 RestTemplate。
     *
     * @return RestTemplate 实例
     */
    @Bean
    public RestTemplate integrationRestTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        return new RestTemplate(requestFactory);
    }

    private ThreadPoolTaskExecutor buildExecutor(String prefix, IntegrationProperties.ThreadPool config) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(prefix);
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());
        executor.setRejectedExecutionHandler(resolveRejectedPolicy(config.getRejectedPolicy()));
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    private ThreadPoolExecutor.AbortPolicy resolveRejectedPolicy(String policy) {
        if (policy == null) {
            return new ThreadPoolExecutor.AbortPolicy();
        }
        if ("CALLER_RUNS".equalsIgnoreCase(policy)) {
            return new ThreadPoolExecutor.AbortPolicy() {
                /**
                 * 被拒绝任务改由调用线程执行。
                 *
                 * @param r 待执行任务
                 * @param e 当前线程池
                 */
                @Override
                public void rejectedExecution(Runnable r, java.util.concurrent.ThreadPoolExecutor e) {
                    if (!e.isShutdown()) {
                        r.run();
                    }
                }
            };
        }
        return new ThreadPoolExecutor.AbortPolicy();
    }
}

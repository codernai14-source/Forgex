package com.forgex.integration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Integration 模块参数配置
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "forgex.integration")
public class IntegrationProperties {

    private Cache cache = new Cache();

    private Queue queue = new Queue();

    private LogBuffer logBuffer = new LogBuffer();

    private ThreadPool syncExecutor = new ThreadPool();

    private ThreadPool asyncConsumerExecutor = new ThreadPool();

    private ThreadPool logExecutor = new ThreadPool();

    private int asyncResultRetentionDays = 7;

    @Data
    public static class Cache {
        private long localExpireSeconds = 300;
        private long redisExpireSeconds = 300;
        private long maxSize = 2000;
    }

    @Data
    public static class Queue {
        private String redisKey = "integration:task:queue";
        private int batchSize = 100;
        private int leaseSeconds = 60;
        private int compensateLimit = 200;
    }

    @Data
    public static class LogBuffer {
        private int batchSize = 100;
        private long flushIntervalMs = 1000;
    }

    @Data
    public static class ThreadPool {
        private int corePoolSize = 8;
        private int maxPoolSize = 32;
        private int queueCapacity = 500;
        private int keepAliveSeconds = 60;
        private String rejectedPolicy = "CALLER_RUNS";
    }
}

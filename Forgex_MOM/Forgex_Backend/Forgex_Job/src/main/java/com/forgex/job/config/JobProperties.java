package com.forgex.job.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Job 调度配置。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "forgex.job")
public class JobProperties {

    private boolean enabled = true;
    private Scheduler scheduler = new Scheduler();
    private Executor executor = new Executor();
    private Instance instance = new Instance();
    private Log log = new Log();
    private Security security = new Security();
    private Workflow workflow = new Workflow();

    @Data
    public static class Scheduler {
        private long scanIntervalMs = 1000;
        private int lookAheadSeconds = 1;
        private long lockWaitMs = 100;
        private long lockLeaseSeconds = 30;
        private long misfireThresholdSeconds = 60;
    }

    @Data
    public static class Executor {
        private int corePoolSize = 10;
        private int maxPoolSize = 50;
        private int queueCapacity = 1000;
        private int shutdownAwaitSeconds = 30;
    }

    @Data
    public static class Instance {
        private long heartbeatIntervalMs = 5000;
        private long offlineThresholdSeconds = 30;
    }

    @Data
    public static class Log {
        private int stackMaxLength = 2000;
        private int retainDays = 30;
    }

    @Data
    public static class Security {
        private List<String> httpWhitelist = new ArrayList<>();
        private List<String> scriptDirectoryWhitelist = new ArrayList<>();
        private boolean scriptEnabled = false;
    }

    @Data
    public static class Workflow {
        private int maxDepth = 100;
    }
}

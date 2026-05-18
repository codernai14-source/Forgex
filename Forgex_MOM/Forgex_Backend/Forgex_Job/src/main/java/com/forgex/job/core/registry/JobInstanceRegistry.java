package com.forgex.job.core.registry;

import com.forgex.job.service.IJobInstanceService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job 实例注册与心跳。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Component
public class JobInstanceRegistry {

    private final IJobInstanceService instanceService;

    public JobInstanceRegistry(IJobInstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        instanceService.registerOrHeartbeat();
    }

    @Scheduled(fixedDelayString = "${forgex.job.instance.heartbeat-interval-ms:5000}")
    public void heartbeat() {
        instanceService.registerOrHeartbeat();
    }
}

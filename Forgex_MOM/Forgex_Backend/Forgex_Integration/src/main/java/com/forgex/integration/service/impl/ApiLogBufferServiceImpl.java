package com.forgex.integration.service.impl;

import com.forgex.integration.domain.entity.ApiCallLog;
import com.forgex.integration.service.IApiCallLogService;
import com.forgex.integration.service.IApiLogBufferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 调用日志批量缓冲服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiLogBufferServiceImpl implements IApiLogBufferService {

    private final IApiCallLogService apiCallLogService;

    private final ConcurrentLinkedQueue<ApiCallLog> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void buffer(ApiCallLog log) {
        if (log != null) {
            queue.offer(log);
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${forgex.integration.log-buffer.flush-interval-ms:1000}")
    public void flush() {
        List<ApiCallLog> batch = new ArrayList<>();
        ApiCallLog item;
        while ((item = queue.poll()) != null) {
            batch.add(item);
            if (batch.size() >= 100) {
                break;
            }
        }
        if (!batch.isEmpty()) {
            apiCallLogService.batchSaveLogs(batch);
        }
    }
}

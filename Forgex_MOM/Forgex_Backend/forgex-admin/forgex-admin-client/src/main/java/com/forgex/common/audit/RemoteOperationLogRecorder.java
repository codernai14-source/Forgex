package com.forgex.common.audit;

import com.forgex.common.audit.OperationLogFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 跨服务操作日志兜底写入器。
 * <p>
 * 当本进程没有本地 {@link OperationLogRecorder}（如 Basic/Report）时，通过 Feign 写入 Sys。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RemoteOperationLogRecorder implements OperationLogRecorder {

    private final OperationLogFeignClient operationLogFeignClient;

    /**
     * 远程写入操作日志。
     *
     * @param record 记录
     */
    @Override
    public void record(OperationLogRecord record) {
        try {
            operationLogFeignClient.record(record);
        } catch (Exception ex) {
            log.warn("remote operation log record failed: {}", ex.getMessage());
        }
    }
}

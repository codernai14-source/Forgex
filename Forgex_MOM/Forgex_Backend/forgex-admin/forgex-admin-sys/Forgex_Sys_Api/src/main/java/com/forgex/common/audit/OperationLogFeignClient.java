package com.forgex.common.audit;

import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 跨服务操作日志写入客户端。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@FeignClient(name = "forgex-sys", contextId = "operationLogFeignClient", path = "/sys/operationLog")
public interface OperationLogFeignClient {

    /**
     * 写入一条操作日志。
     *
     * @param record 记录
     * @return 是否成功
     */
    @PostMapping("/internal/record")
    R<Boolean> record(@RequestBody OperationLogRecord record);
}

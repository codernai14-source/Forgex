package com.forgex.integration.service;

import com.forgex.integration.domain.entity.ApiCallLog;

/**
 * 调用日志缓冲服务
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IApiLogBufferService {

    /**
     * 执行接口日志buffer的buffer操作。
     *
     * @param log 日志
     */
    void buffer(ApiCallLog log);

    /**
     * 执行接口日志buffer的flush操作。
     */
    void flush();
}

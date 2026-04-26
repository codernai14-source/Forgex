package com.forgex.integration.service;

import com.forgex.integration.domain.entity.ApiCallLog;

/**
 * 调用日志缓冲服务
 */
public interface IApiLogBufferService {

    void buffer(ApiCallLog log);

    void flush();
}

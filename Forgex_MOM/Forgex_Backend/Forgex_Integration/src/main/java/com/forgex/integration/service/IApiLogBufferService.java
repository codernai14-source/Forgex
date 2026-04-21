package com.forgex.integration.service;

import com.forgex.integration.domain.entity.ApiCallLog;

/**
 * 璋冪敤鏃ュ織缂撳啿鏈嶅姟
 */
public interface IApiLogBufferService {

    void buffer(ApiCallLog log);

    void flush();
}

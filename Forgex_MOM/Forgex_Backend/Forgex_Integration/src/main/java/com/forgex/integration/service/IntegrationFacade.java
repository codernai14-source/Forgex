package com.forgex.integration.service;

import com.forgex.integration.domain.model.IntegrationExecuteResult;

/**
 * 鍐呴儴鍏叡璋冪敤闂ㄩ潰
 */
public interface IntegrationFacade {

    IntegrationExecuteResult invoke(String apiCode, Object requestEntity);
}

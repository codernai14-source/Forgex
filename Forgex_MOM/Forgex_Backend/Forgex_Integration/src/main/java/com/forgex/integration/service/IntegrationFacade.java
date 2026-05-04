package com.forgex.integration.service;

import com.forgex.integration.domain.model.IntegrationExecuteResult;

/**
 * 内部公共调用门面
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IntegrationFacade {

    /**
     * 执行integrationfacade的调用操作。
     *
     * @param apiCode 接口编码
     * @param requestEntity 请求实体
     * @return 处理结果
     */
    IntegrationExecuteResult invoke(String apiCode, Object requestEntity);
}

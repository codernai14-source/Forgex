package com.forgex.integration.service;

import com.forgex.integration.domain.model.IntegrationExecuteResult;

import java.util.Map;

/**
 * 统一入口执行服务
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IApiGatewayService {

    /**
     * 执行接口gateway的调用inbound操作。
     *
     * @param apiCode 接口编码
     * @param rawPayload 原始payload
     * @param callerIp 调用方IP
     * @return 处理结果
     */
    IntegrationExecuteResult invokeInbound(String apiCode, Map<String, Object> rawPayload, String callerIp);

    /**
     * 执行接口gateway的调用出站操作。
     *
     * @param apiCode 接口编码
     * @param requestEntity 请求实体
     * @return 处理结果
     */
    IntegrationExecuteResult invokeOutbound(String apiCode, Object requestEntity);
}

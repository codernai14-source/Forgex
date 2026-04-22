package com.forgex.integration.service;

import com.forgex.integration.domain.model.IntegrationExecuteResult;

import java.util.Map;

/**
 * 缁熶竴鍏ュ彛鎵ц鏈嶅姟
 */
public interface IApiGatewayService {

    IntegrationExecuteResult invokeInbound(String apiCode, Map<String, Object> rawPayload, String callerIp);

    IntegrationExecuteResult invokeOutbound(String apiCode, Object requestEntity);
}

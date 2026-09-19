package com.forgex.integration.service.impl;

import com.forgex.integration.domain.model.IntegrationExecuteResult;
import com.forgex.integration.service.IApiGatewayService;
import com.forgex.integration.service.IntegrationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 内部公共调用门面实现
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class IntegrationFacadeImpl implements IntegrationFacade {

    private final IApiGatewayService apiGatewayService;

    /**
     * 调用集成接口。
     *
     * @param apiCode 接口编码
     * @param requestEntity 请求实体
     * @return 处理结果
     */
    @Override
    public IntegrationExecuteResult invoke(String apiCode, Object requestEntity) {
        return apiGatewayService.invokeOutbound(apiCode, requestEntity);
    }
}

package com.forgex.integration.service.impl;

import com.forgex.integration.domain.model.IntegrationExecuteResult;
import com.forgex.integration.service.IApiGatewayService;
import com.forgex.integration.service.IntegrationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 鍐呴儴鍏叡璋冪敤闂ㄩ潰瀹炵幇
 */
@Service
@RequiredArgsConstructor
public class IntegrationFacadeImpl implements IntegrationFacade {

    private final IApiGatewayService apiGatewayService;

    @Override
    public IntegrationExecuteResult invoke(String apiCode, Object requestEntity) {
        return apiGatewayService.invokeOutbound(apiCode, requestEntity);
    }
}

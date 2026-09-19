package com.forgex.integration.service;

import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.domain.model.OutboundRequestDefinition;

/**
 * 接口出站executor服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IApiOutboundExecutor {

    /**
     * 执行接口出站executor的执行操作。
     *
     * @param snapshot snapshot
     * @param context context
     * @param requestDefinition 请求definition
     * @return 处理结果
     */
    Object execute(ApiDefinitionSnapshot snapshot, ApiExecutionContext context, OutboundRequestDefinition requestDefinition);
}

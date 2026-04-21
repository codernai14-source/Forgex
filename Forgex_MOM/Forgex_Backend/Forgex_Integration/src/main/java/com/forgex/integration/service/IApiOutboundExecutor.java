package com.forgex.integration.service;

import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.domain.model.OutboundRequestDefinition;

public interface IApiOutboundExecutor {

    Object execute(ApiDefinitionSnapshot snapshot, ApiExecutionContext context, OutboundRequestDefinition requestDefinition);
}

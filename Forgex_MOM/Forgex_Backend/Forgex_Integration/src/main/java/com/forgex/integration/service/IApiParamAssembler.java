package com.forgex.integration.service;

import com.forgex.integration.domain.dto.ApiOutboundTargetDTO;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.OutboundRequestDefinition;

import java.util.Map;

public interface IApiParamAssembler {

    Map<String, Object> assembleInbound(ApiDefinitionSnapshot snapshot, Map<String, Object> rawPayload);

    OutboundRequestDefinition assembleOutbound(ApiDefinitionSnapshot snapshot, Map<String, Object> rawPayload);

    OutboundRequestDefinition assembleOutbound(ApiDefinitionSnapshot snapshot,
                                               Map<String, Object> rawPayload,
                                               ApiOutboundTargetDTO target);
}

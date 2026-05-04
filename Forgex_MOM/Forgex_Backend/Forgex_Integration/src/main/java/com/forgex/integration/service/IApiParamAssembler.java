package com.forgex.integration.service;

import com.forgex.integration.domain.dto.ApiOutboundTargetDTO;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.OutboundRequestDefinition;

import java.util.Map;

/**
 * 接口参数assembler服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IApiParamAssembler {

    /**
     * 执行接口参数assembler的assembleinbound操作。
     *
     * @param snapshot snapshot
     * @param rawPayload 原始payload
     * @return 映射结果
     */
    Map<String, Object> assembleInbound(ApiDefinitionSnapshot snapshot, Map<String, Object> rawPayload);

    /**
     * 执行接口参数assembler的assemble出站操作。
     *
     * @param snapshot snapshot
     * @param rawPayload 原始payload
     * @return 处理结果
     */
    OutboundRequestDefinition assembleOutbound(ApiDefinitionSnapshot snapshot, Map<String, Object> rawPayload);

    OutboundRequestDefinition assembleOutbound(ApiDefinitionSnapshot snapshot,
                                               Map<String, Object> rawPayload,
                                               ApiOutboundTargetDTO target);
}

package com.forgex.integration.domain.model;

import com.forgex.integration.domain.dto.ApiConfigDTO;
import com.forgex.integration.domain.dto.ApiOutboundTargetDTO;
import com.forgex.integration.domain.dto.ApiParamConfigDTO;
import com.forgex.integration.domain.dto.ApiParamMappingDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 接口定义快照
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@Builder
public class ApiDefinitionSnapshot {

    private Long tenantId;

    private String cacheKey;

    private ApiConfigDTO apiConfig;

    private List<ApiParamConfigDTO> requestParamTree;

    private List<ApiParamConfigDTO> responseParamTree;

    private List<ApiParamMappingDTO> inboundMappings;

    private List<ApiParamMappingDTO> outboundMappings;

    private List<ApiOutboundTargetDTO> outboundTargets;
}

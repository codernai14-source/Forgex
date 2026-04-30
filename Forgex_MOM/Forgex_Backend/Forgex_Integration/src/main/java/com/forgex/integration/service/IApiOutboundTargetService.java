package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.dto.ApiOutboundTargetDTO;
import com.forgex.integration.domain.entity.ApiOutboundTarget;

import java.util.List;

public interface IApiOutboundTargetService extends IService<ApiOutboundTarget> {

    List<ApiOutboundTargetDTO> listByApiConfigId(Long apiConfigId);

    List<ApiOutboundTargetDTO> listEnabledByApiConfigId(Long apiConfigId);

    void replaceTargets(Long apiConfigId, List<ApiOutboundTargetDTO> targets);
}

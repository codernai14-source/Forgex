package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.dto.ApiOutboundTargetDTO;
import com.forgex.integration.domain.entity.ApiOutboundTarget;

import java.util.List;

/**
 * 接口出站目标服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IApiOutboundTargetService extends IService<ApiOutboundTarget> {

    /**
     * 执行接口出站目标的列表by接口配置ID操作。
     *
     * @param apiConfigId 接口配置 ID
     * @return 列表数据
     */
    List<ApiOutboundTargetDTO> listByApiConfigId(Long apiConfigId);

    /**
     * 执行接口出站目标的列表启用by接口配置ID操作。
     *
     * @param apiConfigId 接口配置 ID
     * @return 列表数据
     */
    List<ApiOutboundTargetDTO> listEnabledByApiConfigId(Long apiConfigId);

    /**
     * 执行接口出站目标的replacetargets操作。
     *
     * @param apiConfigId 接口配置 ID
     * @param targets targets
     */
    void replaceTargets(Long apiConfigId, List<ApiOutboundTargetDTO> targets);
}

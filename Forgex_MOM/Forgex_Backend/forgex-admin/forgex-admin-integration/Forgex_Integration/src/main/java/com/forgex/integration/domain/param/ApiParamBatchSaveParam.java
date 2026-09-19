package com.forgex.integration.domain.param;

import com.forgex.integration.domain.dto.ApiParamConfigDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 参数树批量保存参数
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class ApiParamBatchSaveParam {

    /**
     * 接口配置 ID
     */
    private Long apiConfigId;

    /**
     * 出站目标 ID。
     */
    private Long outboundTargetId;

    /**
     * 参数方向：REQUEST / RESPONSE
     */
    private String direction;

    /**
     * 参数树
     */
    private List<ApiParamConfigDTO> tree = new ArrayList<>();
}

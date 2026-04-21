package com.forgex.integration.domain.param;

import com.forgex.integration.domain.dto.ApiParamConfigDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 参数树批量保存参数
 */
@Data
public class ApiParamBatchSaveParam {

    /**
     * 接口配置 ID
     */
    private Long apiConfigId;

    /**
     * 参数方向：REQUEST / RESPONSE
     */
    private String direction;

    /**
     * 参数树
     */
    private List<ApiParamConfigDTO> tree = new ArrayList<>();
}

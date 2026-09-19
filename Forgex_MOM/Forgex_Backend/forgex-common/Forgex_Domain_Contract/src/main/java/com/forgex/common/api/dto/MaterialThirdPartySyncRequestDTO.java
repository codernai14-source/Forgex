package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 物料第三方同步请求 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-12
 */
@Data
public class MaterialThirdPartySyncRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前租户 ID。
     */
    private Long tenantId;

    /**
     * 物料聚合列表。
     */
    private List<MaterialAggregateDTO> materials = new ArrayList<>();
}

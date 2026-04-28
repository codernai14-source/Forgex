package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 供应商第三方同步请求 DTO
 * <p>
 * 用于接口平台与基础数据模块之间传递供应商全量同步数据。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@Data
public class SupplierThirdPartySyncRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前租户 ID
     */
    private Long tenantId;

    /**
     * 供应商聚合列表
     */
    private List<SupplierAggregateDTO> suppliers = new ArrayList<>();
}

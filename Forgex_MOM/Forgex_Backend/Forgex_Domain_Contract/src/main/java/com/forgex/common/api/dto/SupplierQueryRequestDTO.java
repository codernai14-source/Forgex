package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 供应商内部查询请求 DTO
 * <p>
 * 用于基础数据内部供应商聚合查询和供应商下拉查询。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@Data
public class SupplierQueryRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 单个供应商编码
     */
    private String supplierCode;

    /**
     * 供应商编码列表
     */
    private List<String> supplierCodes = new ArrayList<>();

    /**
     * 是否仅查询可用供应商
     */
    private Boolean availableOnly;
}

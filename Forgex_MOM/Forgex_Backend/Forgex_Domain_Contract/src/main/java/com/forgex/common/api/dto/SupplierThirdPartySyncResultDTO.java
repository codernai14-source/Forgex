package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 供应商第三方同步结果 DTO
 * <p>
 * 统一封装供应商第三方同步创建、更新和失败统计结果。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@Data
public class SupplierThirdPartySyncResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 同步总数
     */
    private Integer totalCount = 0;

    /**
     * 创建数量
     */
    private Integer createdCount = 0;

    /**
     * 更新数量
     */
    private Integer updatedCount = 0;

    /**
     * 失败数量
     */
    private Integer failedCount = 0;

    /**
     * 失败的供应商编码列表
     */
    private List<String> failedSupplierCodes = new ArrayList<>();
}

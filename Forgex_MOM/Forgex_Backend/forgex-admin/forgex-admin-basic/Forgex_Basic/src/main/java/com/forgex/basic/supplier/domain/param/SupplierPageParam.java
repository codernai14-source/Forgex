package com.forgex.basic.supplier.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商分页查询参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "供应商分页查询参数")
public class SupplierPageParam extends BaseGetParam {

    /**
     * 供应商编码。
     */
    @Schema(description = "供应商编码")
    private String supplierCode;

    /**
     * 供应商名称，兼容旧前端字段。
     */
    @Schema(description = "供应商名称")
    private String supplierName;

    /**
     * 供应商全称。
     */
    @Schema(description = "供应商全称")
    private String supplierFullName;

    /**
     * 合作状态。
     */
    @Schema(description = "合作状态")
    private String cooperationStatus;

    /**
     * 信用等级。
     */
    @Schema(description = "信用等级")
    private String creditLevel;

    /**
     * 风险等级。
     */
    @Schema(description = "风险等级")
    private String riskLevel;

    /**
     * 供应商分级。
     */
    @Schema(description = "供应商分级")
    private String supplierLevel;

    /**
     * 关联租户编码。
     */
    @Schema(description = "关联租户编码")
    private String relatedTenantCode;

    /**
     * 审查状态。
     */
    @Schema(description = "审查状态")
    private Integer reviewStatus;
}

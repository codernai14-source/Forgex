package com.forgex.basic.supplier.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 供应商资质审查发起参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
@Data
@Schema(description = "供应商资质审查发起参数")
public class SupplierReviewStartParam {

    /**
     * 供应商 ID。
     */
    @Schema(description = "供应商 ID")
    private Long supplierId;

    /**
     * 发起人自选审批人。
     */
    @Schema(description = "发起人自选审批人")
    private List<Long> selectedApprovers = new ArrayList<>();
}

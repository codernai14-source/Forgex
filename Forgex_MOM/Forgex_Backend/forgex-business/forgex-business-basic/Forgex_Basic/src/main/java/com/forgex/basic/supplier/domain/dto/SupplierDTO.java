package com.forgex.basic.supplier.domain.dto;

import com.forgex.common.api.dto.SupplierAggregateDTO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 供应商聚合返回对象。
 * <p>
 * 继承公共聚合 DTO，保留原有 {@code SupplierDTO} 类型名，降低控制器与前端适配成本。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
@Schema(description = "供应商聚合信息")
public class SupplierDTO extends SupplierAggregateDTO {
}

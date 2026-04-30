package com.forgex.basic.customer.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户主数据分页查询参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户主数据分页查询参数")
public class CustomerPageParam extends BaseGetParam {
    private String customerCode;
    private String customerName;
    private String customerFullName;
    private String customerValueLevel;
    private String customerCreditLevel;
    private String businessStatus;
    private Integer approvalStatus;
    private Boolean isRelatedTenant;
    private Integer status;
}

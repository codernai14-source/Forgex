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
    /**
     * 客户编码。
     */
    private String customerCode;
    /**
     * 客户名称。
     */
    private String customerName;
    /**
     * 客户全称。
     */
    private String customerFullName;
    /**
     * 客户价值等级。
     */
    private String customerValueLevel;
    /**
     * 客户信用等级。
     */
    private String customerCreditLevel;
    /**
     * 经营状态。
     */
    private String businessStatus;
    /**
     * 审批状态。
     */
    private Integer approvalStatus;
    /**
     * 是否关联租户。
     */
    private Boolean isRelatedTenant;
    /**
     * 状态。
     */
    private Integer status;
}

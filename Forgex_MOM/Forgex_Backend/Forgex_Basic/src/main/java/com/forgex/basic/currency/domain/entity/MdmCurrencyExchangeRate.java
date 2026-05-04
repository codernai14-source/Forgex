package com.forgex.basic.currency.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 汇率明细实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mdm_currency_exchange_rate")
public class MdmCurrencyExchangeRate extends BaseEntity {
    /**
     * 来源币种编码。
     */
    @TableField("source_currency_code")
    private String sourceCurrencyCode;
    /**
     * 目标币种编码。
     */
    @TableField("target_currency_code")
    private String targetCurrencyCode;
    /**
     * 汇率类型编码。
     */
    @TableField("rate_type_code")
    private String rateTypeCode;
    /**
     * 生效日期。
     */
    @TableField("effective_date")
    private LocalDate effectiveDate;
    /**
     * 汇率值。
     */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
     * 失效日期。
     */
    @TableField("expire_date")
    private LocalDate expireDate;
    /**
     * 审批状态。
     */
    @TableField("approve_status")
    private Integer approveStatus;
    /**
     * 审批人。
     */
    @TableField("approve_user")
    private String approveUser;
    /**
     * 审批时间。
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
     * org ID。
     */
    @TableField("org_id")
    private Long orgId;
    /**
     * 备注。
     */
    @TableField("remark")
    private String remark;
}

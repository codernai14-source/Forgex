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
    @TableField("source_currency_code")
    private String sourceCurrencyCode;
    @TableField("target_currency_code")
    private String targetCurrencyCode;
    @TableField("rate_type_code")
    private String rateTypeCode;
    @TableField("effective_date")
    private LocalDate effectiveDate;
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    @TableField("expire_date")
    private LocalDate expireDate;
    @TableField("approve_status")
    private Integer approveStatus;
    @TableField("approve_user")
    private String approveUser;
    @TableField("approve_time")
    private LocalDateTime approveTime;
    @TableField("org_id")
    private Long orgId;
    @TableField("remark")
    private String remark;
}

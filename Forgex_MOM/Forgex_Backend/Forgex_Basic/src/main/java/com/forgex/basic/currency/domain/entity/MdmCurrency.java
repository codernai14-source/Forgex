package com.forgex.basic.currency.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 币种主数据实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mdm_currency")
public class MdmCurrency extends BaseEntity {
    @TableField("currency_code")
    private String currencyCode;
    @TableField("currency_num_code")
    private String currencyNumCode;
    @TableField("currency_name_cn")
    private String currencyNameCn;
    @TableField("currency_name_en")
    private String currencyNameEn;
    @TableField("currency_symbol")
    private String currencySymbol;
    @TableField("decimal_digits")
    private Integer decimalDigits;
    @TableField("is_base_currency")
    private Boolean isBaseCurrency;
    @TableField("country_region")
    private String countryRegion;
    @TableField("status")
    private Integer status;
    @TableField("remark")
    private String remark;
}

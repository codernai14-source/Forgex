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
    /**
     * 币种字母编码。
     */
    @TableField("currency_code")
    private String currencyCode;
    /**
     * 币种数字编码。
     */
    @TableField("currency_num_code")
    private String currencyNumCode;
    /**
     * 币种中文名称。
     */
    @TableField("currency_name_cn")
    private String currencyNameCn;
    /**
     * 币种英文名称。
     */
    @TableField("currency_name_en")
    private String currencyNameEn;
    /**
     * 币种符号。
     */
    @TableField("currency_symbol")
    private String currencySymbol;
    /**
     * 小数位数。
     */
    @TableField("decimal_digits")
    private Integer decimalDigits;
    /**
     * 是否本位币。
     */
    @TableField("is_base_currency")
    private Boolean isBaseCurrency;
    /**
     * 国家或地区。
     */
    @TableField("country_region")
    private String countryRegion;
    /**
     * 状态。
     */
    @TableField("status")
    private Integer status;
    /**
     * 备注。
     */
    @TableField("remark")
    private String remark;
}

package com.forgex.basic.currency.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 汇率类型实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mdm_exchange_rate_type")
public class MdmExchangeRateType extends BaseEntity {
    @TableField("rate_type_code")
    private String rateTypeCode;
    @TableField("rate_type_name")
    private String rateTypeName;
    @TableField("business_scene")
    private String businessScene;
    @TableField("is_default")
    private Boolean isDefault;
    @TableField("status")
    private Integer status;
}

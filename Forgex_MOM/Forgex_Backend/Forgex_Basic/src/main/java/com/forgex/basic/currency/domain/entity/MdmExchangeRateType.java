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
    /**
     * 汇率类型编码。
     */
    @TableField("rate_type_code")
    private String rateTypeCode;
    /**
     * 汇率类型名称。
     */
    @TableField("rate_type_name")
    private String rateTypeName;
    /**
     * 业务scene。
     */
    @TableField("business_scene")
    private String businessScene;
    /**
     * 是否默认。
     */
    @TableField("is_default")
    private Boolean isDefault;
    /**
     * 状态。
     */
    @TableField("status")
    private Integer status;
}

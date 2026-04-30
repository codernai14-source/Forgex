package com.forgex.basic.currency.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 汇率操作日志实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mdm_exchange_rate_log")
public class MdmExchangeRateLog extends BaseEntity {
    @TableField("rate_id")
    private Long rateId;
    @TableField("operation_type")
    private String operationType;
    @TableField("operation_content")
    private String operationContent;
    @TableField("operator_ip")
    private String operatorIp;
}

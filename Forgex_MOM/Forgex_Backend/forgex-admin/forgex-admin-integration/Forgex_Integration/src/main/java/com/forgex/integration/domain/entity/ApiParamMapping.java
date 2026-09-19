package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口参数映射实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_param_mapping")
public class ApiParamMapping extends BaseEntity {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接口配置 ID。
     */
    private Long apiConfigId;

    /**
     * 出站目标 ID。
     */
    private Long outboundTargetId;

    /**
     * 来源字段路径。
     */
    private String sourceFieldPath;

    /**
     * 目标字段路径。
     */
    private String targetFieldPath;

    /**
     * 转换规则。
     */
    private String transformRule;

    /**
     * 默认价值。
     */
    private String defaultValue;

    /**
     * 常量值。
     */
    private String constantValue;

    /**
     * BODY / QUERY / HEADER / PATH
     */
    private String targetScope;

    /**
     * SOURCE / DEFAULT / CONSTANT
     */
    private String valueType;

    /**
     * 方向。
     */
    private String direction;

    /**
     * 备注。
     */
    private String remark;
}

package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口参数映射实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_param_mapping")
public class ApiParamMapping extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long apiConfigId;

    private String sourceFieldPath;

    private String targetFieldPath;

    private String transformRule;

    private String defaultValue;

    private String constantValue;

    /**
     * BODY / QUERY / HEADER / PATH
     */
    private String targetScope;

    /**
     * SOURCE / DEFAULT / CONSTANT
     */
    private String valueType;

    private String direction;

    private String remark;
}

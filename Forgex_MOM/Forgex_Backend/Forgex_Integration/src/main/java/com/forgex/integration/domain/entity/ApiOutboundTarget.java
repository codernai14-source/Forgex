package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_outbound_target")
public class ApiOutboundTarget extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long apiConfigId;

    private Long thirdSystemId;

    private String targetCode;

    private String targetName;

    private String targetUrl;

    private String httpMethod;

    private String contentType;

    private String invokeMode;

    private Integer timeoutMs;

    private Integer retryCount;

    private Integer retryIntervalMs;

    private Integer orderNum;

    private Integer status;

    private String remark;
}

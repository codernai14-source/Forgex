package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口配置实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_config")
public class ApiConfig extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String apiCode;

    private String apiName;

    private String apiDesc;

    /**
     * INBOUND / OUTBOUND
     */
    private String direction;

    private String apiPath;

    private String processorBean;

    /**
     * HTTP / TCP
     */
    private String callMethod;

    /**
     * GET / POST / PUT / DELETE
     */
    private String httpMethod;

    /**
     * SYNC / ASYNC
     */
    private String invokeMode;

    private String contentType;

    private String targetUrl;

    private Integer timeoutMs;

    private Integer retryCount;

    private Integer retryIntervalMs;

    private Integer maxConcurrent;

    private Integer queueLimit;

    /**
     * NONE / TOKEN / BASIC / CUSTOM
     */
    private String authType;

    /**
     * JSON config
     */
    private String authConfig;

    private Long callCount;

    private Integer status;

    private String moduleCode;
}

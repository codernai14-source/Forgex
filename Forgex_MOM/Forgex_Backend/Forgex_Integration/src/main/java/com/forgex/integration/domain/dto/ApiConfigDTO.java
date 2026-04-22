package com.forgex.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接口配置 DTO。
 */
@Data
public class ApiConfigDTO {

    private Long id;

    private String apiCode;

    private String apiName;

    private String apiDesc;

    private String direction;

    private String apiPath;

    private String processorBean;

    private String callMethod;

    private String httpMethod;

    private String invokeMode;

    private String contentType;

    private String targetUrl;

    private Integer timeoutMs;

    private Integer retryCount;

    private Integer retryIntervalMs;

    private Integer maxConcurrent;

    private Integer queueLimit;

    private String authType;

    private String authConfig;

    private Long callCount;

    private Integer status;

    private String moduleCode;

    private List<ApiOutboundTargetDTO> outboundTargets;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

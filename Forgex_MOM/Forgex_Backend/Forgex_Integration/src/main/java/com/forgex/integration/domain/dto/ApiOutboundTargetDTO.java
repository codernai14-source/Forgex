package com.forgex.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiOutboundTargetDTO {

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

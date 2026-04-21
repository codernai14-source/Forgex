package com.forgex.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口调用日志 DTO。
 */
@Data
public class ApiCallLogDTO {

    private Long id;

    private Long apiConfigId;

    private String apiCode;

    private String callDirection;

    private String callerIp;

    private String traceId;

    private String taskId;

    private String invokeMode;

    private String requestData;

    private String rawRequestData;

    private String assembledRequestData;

    private String responseData;

    private String responseCode;

    private String callStatus;

    private String resultType;

    private String errorMessage;

    private Integer costTimeMs;

    private LocalDateTime callTime;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}

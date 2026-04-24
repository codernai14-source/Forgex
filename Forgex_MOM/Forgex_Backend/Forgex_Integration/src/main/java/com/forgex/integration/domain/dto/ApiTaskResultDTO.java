package com.forgex.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiTaskResultDTO {

    private Long id;

    private String taskId;

    private String traceId;

    private Long apiConfigId;

    private String apiCode;

    private String direction;

    private String status;

    private String resultType;

    private String resultData;

    private String errorMessage;

    private Integer costTimeMs;

    private LocalDateTime finishedTime;

    private LocalDateTime expireTime;
}

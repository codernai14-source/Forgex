package com.forgex.integration.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 寮傛浠诲姟缁撴灉杩斿洖
 */
@Data
@Builder
public class ApiTaskResultVO {

    private String taskId;

    private String status;

    private String resultType;

    private String resultData;

    private String errorMessage;

    private LocalDateTime finishedTime;
}

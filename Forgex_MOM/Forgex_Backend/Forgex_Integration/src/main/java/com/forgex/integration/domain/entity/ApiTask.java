package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_task")
public class ApiTask extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String taskId;

    private String traceId;

    private Long apiConfigId;

    private String apiCode;

    private String direction;

    private String processorBean;

    private String invokeMode;

    private String requestPayload;

    private String assembledPayload;

    private String status;

    private Integer retryCount;

    private Integer maxRetryCount;

    private LocalDateTime nextExecuteTime;

    private LocalDateTime leaseExpireTime;

    private LocalDateTime startedTime;

    private LocalDateTime finishedTime;

    private String resultType;

    private String errorMessage;
}

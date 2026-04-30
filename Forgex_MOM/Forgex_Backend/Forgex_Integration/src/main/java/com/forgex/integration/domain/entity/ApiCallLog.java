package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 接口调用日志实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_call_log")
public class ApiCallLog extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long apiConfigId;

    private Long outboundTargetId;

    private String targetSystemCode;

    private String targetSystemName;

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
}

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
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_call_log")
public class ApiCallLog extends BaseEntity {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接口配置 ID。
     */
    private Long apiConfigId;

    /**
     * 出站目标 ID。
     */
    private Long outboundTargetId;

    /**
     * 目标系统编码。
     */
    private String targetSystemCode;

    /**
     * 目标系统名称。
     */
    private String targetSystemName;

    /**
     * 接口编码。
     */
    private String apiCode;

    /**
     * 调用方向。
     */
    private String callDirection;

    /**
     * 调用方IP。
     */
    private String callerIp;

    /**
     * 链路 ID。
     */
    private String traceId;

    /**
     * 任务 ID。
     */
    private String taskId;

    /**
     * 调用模式。
     */
    private String invokeMode;

    /**
     * 请求数据。
     */
    private String requestData;

    /**
     * 原始请求数据。
     */
    private String rawRequestData;

    /**
     * 组装后请求数据。
     */
    private String assembledRequestData;

    /**
     * 响应数据。
     */
    private String responseData;

    /**
     * 响应编码。
     */
    private String responseCode;

    /**
     * 调用状态。
     */
    private String callStatus;

    /**
     * 结果类型。
     */
    private String resultType;

    /**
     * 错误消息。
     */
    private String errorMessage;

    /**
     * 耗时时间ms。
     */
    private Integer costTimeMs;

    /**
     * 调用时间。
     */
    private LocalDateTime callTime;
}

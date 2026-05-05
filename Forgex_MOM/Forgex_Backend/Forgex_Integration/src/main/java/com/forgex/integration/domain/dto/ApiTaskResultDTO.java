package com.forgex.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口任务结果数据传输对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class ApiTaskResultDTO {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 任务 ID。
     */
    private String taskId;

    /**
     * 链路 ID。
     */
    private String traceId;

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
     * 方向。
     */
    private String direction;

    /**
     * 状态。
     */
    private String status;

    /**
     * 结果类型。
     */
    private String resultType;

    /**
     * 结果数据。
     */
    private String resultData;

    /**
     * 错误消息。
     */
    private String errorMessage;

    /**
     * 耗时时间ms。
     */
    private Integer costTimeMs;

    /**
     * 完成时间。
     */
    private LocalDateTime finishedTime;

    /**
     * 过期时间。
     */
    private LocalDateTime expireTime;
}

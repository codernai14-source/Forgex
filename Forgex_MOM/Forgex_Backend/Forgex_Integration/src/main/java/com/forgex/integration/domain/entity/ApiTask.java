package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 接口任务实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_task")
public class ApiTask extends BaseEntity {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 处理器 Bean 名称。
     */
    private String processorBean;

    /**
     * 调用模式。
     */
    private String invokeMode;

    /**
     * 请求payload。
     */
    private String requestPayload;

    /**
     * 组装后payload。
     */
    private String assembledPayload;

    /**
     * 状态。
     */
    private String status;

    /**
     * 重试次数。
     */
    private Integer retryCount;

    /**
     * maxretrycount。
     */
    private Integer maxRetryCount;

    /**
     * next执行时间。
     */
    private LocalDateTime nextExecuteTime;

    /**
     * leaseexpire时间。
     */
    private LocalDateTime leaseExpireTime;

    /**
     * started时间。
     */
    private LocalDateTime startedTime;

    /**
     * 完成时间。
     */
    private LocalDateTime finishedTime;

    /**
     * 结果类型。
     */
    private String resultType;

    /**
     * 错误消息。
     */
    private String errorMessage;
}

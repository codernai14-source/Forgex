package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作流超时扫描请求。
 * <p>
 * 由定时任务模块跨服务调用工作流模块，触发对已超时待办审批实例的扫描与处理。
 * 定时任务线程没有请求头，租户上下文需通过请求体显式携带。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-07-07
 */
@Data
public class WorkflowTimeoutScanRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户 ID。
     */
    private Long tenantId;

    /**
     * 执行单 ID；为空时扫描该租户下全部超时待办实例。
     */
    private Long executionId;
}

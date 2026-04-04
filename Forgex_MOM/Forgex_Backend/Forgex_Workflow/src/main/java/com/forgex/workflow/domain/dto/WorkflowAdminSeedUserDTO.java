package com.forgex.workflow.domain.dto;

import lombok.Data;

/**
 * 工作流演示审批管理员种子数据 DTO。
 */
@Data
public class WorkflowAdminSeedUserDTO {

    /**
     * 租户 ID。
     */
    private Long tenantId;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 账号。
     */
    private String account;

    /**
     * 用户名。
     */
    private String username;
}

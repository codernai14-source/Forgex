package com.forgex.workflow.domain.dto;

import lombok.Data;

/**
 * 审批工作台发起人审批数量占比项。
 *
 * @author Forgex Team
 * @since 2026-04-10
 */
@Data
public class WfDashboardUserShareDTO {

    /**
     * 发起人 ID。
     */
    private Long initiatorId;

    /**
     * 发起人名称。
     */
    private String initiatorName;

    /**
     * 审批单数量。
     */
    private Long count;
}

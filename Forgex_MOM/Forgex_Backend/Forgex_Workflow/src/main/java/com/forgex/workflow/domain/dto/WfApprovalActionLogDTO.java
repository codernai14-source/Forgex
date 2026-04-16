package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批动作日志数据传输对象。
 * <p>
 * 用于前端展示审批轨迹、操作审计以及补偿中心定位历史动作。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 */
@Data
public class WfApprovalActionLogDTO {

    /**
     * 日志 ID。
     */
    private Long id;

    /**
     * 执行单 ID。
     */
    private Long executionId;

    /**
     * 执行明细 ID。
     */
    private Long executionDetailId;

    /**
     * 节点 ID。
     */
    private Long nodeId;

    /**
     * 审批实例 ID。
     */
    private Long approvalInstanceId;

    /**
     * 动作类型。
     */
    private Integer actionType;

    /**
     * 操作人 ID。
     */
    private Long operatorId;

    /**
     * 操作人名称。
     */
    private String operatorName;

    /**
     * 目标用户 ID。
     */
    private Long targetUserId;

    /**
     * 目标用户名称。
     */
    private String targetUserName;

    /**
     * 动作备注。
     */
    private String actionComment;

    /**
     * 动作快照。
     */
    private String actionSnapshot;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}

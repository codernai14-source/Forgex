package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批实例数据传输对象。
 * <p>
 * 用于前端展示审批实例轨迹、待办归属、补偿中心排查等场景。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 */
@Data
public class WfApprovalInstanceDTO {

    /**
     * 审批实例 ID。
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
     * 当前节点 ID。
     */
    private Long nodeId;

    /**
     * 实例编号。
     */
    private String instanceNo;

    /**
     * 当前审批人 ID。
     */
    private Long approverId;

    /**
     * 当前审批人名称。
     */
    private String approverName;

    /**
     * 审批人来源类型。
     */
    private Integer approverSourceType;

    /**
     * 来源规则 ID。
     */
    private Long sourceRuleId;

    /**
     * 实例状态。
     */
    private Integer status;

    /**
     * 最近一次动作类型。
     */
    private Integer actionType;

    /**
     * 审批备注。
     */
    private String comment;

    /**
     * 审批处理时间。
     */
    private LocalDateTime approveTime;

    /**
     * 超时截止时间。
     */
    private LocalDateTime deadlineTime;

    /**
     * 是否已激活。
     */
    private Boolean activated;

    /**
     * 委托来源用户 ID。
     */
    private Long delegateFromUserId;

    /**
     * 转交来源用户 ID。
     */
    private Long transferFromUserId;
}

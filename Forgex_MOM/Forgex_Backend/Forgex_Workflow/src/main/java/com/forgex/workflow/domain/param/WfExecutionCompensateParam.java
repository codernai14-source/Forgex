package com.forgex.workflow.domain.param;

import lombok.Data;

/**
 * 审批补偿与超时重试参数。
 * <p>
 * 用于补偿中心、超时任务重试等治理场景，支持按执行单、节点、实例和时间范围进行筛选。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 */
@Data
public class WfExecutionCompensateParam {

    /**
     * 执行单 ID。
     */
    private Long executionId;

    /**
     * 节点 ID。
     */
    private Long nodeId;

    /**
     * 审批实例 ID。
     */
    private Long approvalInstanceId;

    /**
     * 查询开始时间。
     */
    private String timeBegin;

    /**
     * 查询结束时间。
     */
    private String timeEnd;
}

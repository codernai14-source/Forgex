package com.forgex.workflow.domain.param;

import lombok.Data;

/**
 * 按执行单 ID 操作的请求参数。
 * <p>
 * 用于抄送已读标记、按执行单查询抄送人等只需主键的接口。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-09-11
 * @see com.forgex.workflow.service.IWfExecutionService
 */
@Data
public class WfExecutionIdParam {

    /**
     * 审批执行单 ID。
     */
    private Long executionId;
}

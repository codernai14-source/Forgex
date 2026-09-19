package com.forgex.basic.customer.domain.param;

import lombok.Data;

import java.util.List;

/**
 * 客户审批start请求参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class CustomerApprovalStartParam {
    /**
     * 客户 ID。
     */
    private Long customerId;
    /**
     * 发起人选择的审批人列表。
     */
    private List<Long> selectedApprovers;
}

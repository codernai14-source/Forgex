package com.forgex.basic.currency.domain.param;

import lombok.Data;

import java.util.List;

/**
 * 汇率审批start请求参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class ExchangeRateApprovalStartParam {
    /**
     * 汇率 ID。
     */
    private Long rateId;
    /**
     * 发起人选择的审批人列表。
     */
    private List<Long> selectedApprovers;
}

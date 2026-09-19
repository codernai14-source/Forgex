package com.forgex.basic.currency.domain.param;

import lombok.Data;

/**
 * 汇率工作流回调请求参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class ExchangeRateWorkflowCallbackParam {
    /**
     * 任务编码。
     */
    private String taskCode;
    /**
     * 状态。
     */
    private Integer status;
    /**
     * 表单内容。
     */
    private String formContent;
}

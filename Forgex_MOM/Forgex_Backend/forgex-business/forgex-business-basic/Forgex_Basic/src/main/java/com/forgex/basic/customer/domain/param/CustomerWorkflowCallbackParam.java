package com.forgex.basic.customer.domain.param;

import lombok.Data;

/**
 * 客户工作流回调请求参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class CustomerWorkflowCallbackParam {
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

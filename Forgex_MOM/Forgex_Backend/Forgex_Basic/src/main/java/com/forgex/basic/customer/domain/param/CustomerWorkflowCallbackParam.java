package com.forgex.basic.customer.domain.param;

import lombok.Data;

@Data
public class CustomerWorkflowCallbackParam {
    private String taskCode;
    private Integer status;
    private String formContent;
}

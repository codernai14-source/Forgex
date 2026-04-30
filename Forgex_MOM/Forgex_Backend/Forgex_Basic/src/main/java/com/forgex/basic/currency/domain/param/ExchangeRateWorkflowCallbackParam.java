package com.forgex.basic.currency.domain.param;

import lombok.Data;

@Data
public class ExchangeRateWorkflowCallbackParam {
    private String taskCode;
    private Integer status;
    private String formContent;
}

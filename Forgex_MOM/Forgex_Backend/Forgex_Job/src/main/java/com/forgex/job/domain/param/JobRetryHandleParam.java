package com.forgex.job.domain.param;

import lombok.Data;

/**
 * 死信处理参数。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
public class JobRetryHandleParam {
    private Long id;
    private Integer action;
    private String remark;
}

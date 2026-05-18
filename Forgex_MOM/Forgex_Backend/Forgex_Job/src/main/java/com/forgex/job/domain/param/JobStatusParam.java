package com.forgex.job.domain.param;

import lombok.Data;

/**
 * 任务状态参数。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
public class JobStatusParam {
    private Long id;
    private Integer status;
}

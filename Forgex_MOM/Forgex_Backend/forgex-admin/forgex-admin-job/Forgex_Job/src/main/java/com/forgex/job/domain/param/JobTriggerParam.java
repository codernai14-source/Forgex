package com.forgex.job.domain.param;

import lombok.Data;

/**
 * 手动触发参数。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
public class JobTriggerParam {
    private Long id;
    private String requestId;
    private String params;
}

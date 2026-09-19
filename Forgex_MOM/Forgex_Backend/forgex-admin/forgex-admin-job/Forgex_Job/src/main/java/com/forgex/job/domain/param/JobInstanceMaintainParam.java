package com.forgex.job.domain.param;

import lombok.Data;

/**
 * 实例维护状态参数。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
public class JobInstanceMaintainParam {
    private Long id;
    private String instanceId;
    private Integer maintenance;
}

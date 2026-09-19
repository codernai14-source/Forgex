package com.forgex.job.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Job 通用分页查询参数。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JobPageParam extends BaseGetParam {
    private Long id;
    private Long jobId;
    private Long workflowId;
    private String jobCode;
    private String jobName;
    private String jobGroup;
    private Integer jobType;
    private Integer scheduleType;
    private Integer status;
    private Integer triggerType;
    private String instanceId;
    private String ruleName;
    private String workflowCode;
    private String workflowName;
}

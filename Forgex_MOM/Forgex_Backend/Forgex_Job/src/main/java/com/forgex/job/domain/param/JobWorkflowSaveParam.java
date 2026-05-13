package com.forgex.job.domain.param;

import lombok.Data;

/**
 * DAG 工作流保存参数。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
public class JobWorkflowSaveParam {
    private Long id;
    private String workflowCode;
    private String workflowName;
    private Integer status;
    private String graphJson;
    private String remark;
}

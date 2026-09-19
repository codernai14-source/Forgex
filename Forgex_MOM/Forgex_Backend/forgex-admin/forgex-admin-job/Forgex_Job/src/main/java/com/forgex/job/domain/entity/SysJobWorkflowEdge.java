package com.forgex.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DAG 连线实体。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job_workflow_edge")
public class SysJobWorkflowEdge extends BaseEntity {
    private Long workflowId;
    private String edgeCode;
    private String sourceNodeCode;
    private String targetNodeCode;
    private String conditionExpression;
}

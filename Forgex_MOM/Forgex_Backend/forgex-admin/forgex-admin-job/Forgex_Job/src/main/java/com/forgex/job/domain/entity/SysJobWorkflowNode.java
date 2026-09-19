package com.forgex.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DAG 节点实体。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job_workflow_node")
public class SysJobWorkflowNode extends BaseEntity {
    private Long workflowId;
    private String nodeCode;
    private String nodeName;
    private Long jobId;
    private String nodeType;
    private String nodeConfig;
    private Integer positionX;
    private Integer positionY;
}

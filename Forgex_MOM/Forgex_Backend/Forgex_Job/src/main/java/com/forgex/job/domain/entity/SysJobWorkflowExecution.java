package com.forgex.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * DAG 执行记录实体。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job_workflow_execution")
public class SysJobWorkflowExecution extends BaseEntity {
    private Long workflowId;
    private String workflowCode;
    private Long rootLogId;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String nodeStatusJson;
    private String resultMessage;
}

package com.forgex.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DAG 工作流定义实体。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job_workflow")
public class SysJobWorkflow extends BaseEntity {
    private String workflowCode;
    private String workflowName;
    private Integer status;
    private String graphJson;
    private String remark;
}

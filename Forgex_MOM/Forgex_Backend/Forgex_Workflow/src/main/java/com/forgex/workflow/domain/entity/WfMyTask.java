/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的待办实体。
 * <p>
 * 对应表 {@code wf_my_task}，用于记录用户个人的待办审批任务。
 * </p>
 *
 * <p>状态说明：</p>
 * <ul>
 *   <li>0=待审批</li>
 *   <li>1=已审批</li>
 *   <li>2=已驳回</li>
 *   <li>3=已转交</li>
 *   <li>4=已委托</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see WfTaskExecution 审批任务执行实体
 * @see WfTaskApprovalInstance 审批实例实体
 */
@Data
@TableName("wf_my_task")
public class WfMyTask {

    /**
     * 主键 ID。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批任务执行表 ID。
     * <p>
     * 关联 {@code wf_task_execution} 表的主键。
     * </p>
     *
     * @see WfTaskExecution#id
     */
    private Long executionId;

    /**
     * 审批任务名称。
     */
    private String taskName;

    /**
     * 审批节点 ID。
     * <p>
     * 关联 {@code wf_task_node_config} 表的主键。
     * </p>
     *
     * @see WfTaskNodeConfig#id
     */
    private Long nodeId;

    /**
     * 审批节点名称。
     */
    private String nodeName;

    /**
     * 审批实例 ID。
     * <p>
     * 关联 {@code wf_task_approval_instance} 表的主键。
     * </p>
     *
     * @see WfTaskApprovalInstance#id
     */
    private Long approvalInstanceId;

    /**
     * 审批任务执行详情表 ID。
     * <p>
     * 关联 {@code wf_task_execution_detail} 表的主键。
     * </p>
     *
     * @see WfTaskExecutionDetail#id
     */
    private Long executionDetailId;

    /**
     * 当前审批人 ID。
     * <p>
     * 关联系统用户表的主键。
     * </p>
     */
    private Long approverId;

    /**
     * 审批人 ID 集合（JSON 数组）。
     * <p>
     * 会签或或签场景下，存储该节点所有审批人的 ID 集合。
     * 格式：[10001, 10002, 10003]
     * </p>
     */
    private String approverIds;

    /**
     * 审批状态。
     * <p>
     * 0=待审批<br>
     * 1=已审批<br>
     * 2=已驳回<br>
     * 3=已转交<br>
     * 4=已委托
     * </p>
     */
    private Integer status;

    /**
     * 租户 ID。
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 审批截止时间。
     */
    @TableField("deadline_time")
    private LocalDateTime deadlineTime;
}

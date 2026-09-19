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

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批任务执行人详情实体。
 * <p>
 * 映射表：{@code wf_task_execution_approver}。用于记录审批人的审批详情。
 * </p>
 *
 * <p>驳回类型：</p>
 * <ul>
 *   <li>1=驳回任务：直接结束整个审批任务</li>
 *   <li>2=返回上一节点：退回到上一个审批节点</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
@TableName("wf_task_execution_approver")
public class WfTaskExecutionApprover {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批任务执行详情表ID
     */
    private Long executionDetailId;

    /**
     * 审批任务执行表ID
     */
    private Long executionId;

    /**
     * 审批任务节点ID
     */
    private Long nodeId;

    /**
     * 审批人详情（JSON）
     * <p>
     * 格式：
     * [
     *   {
     *     "approverId": 10001,
     *     "approverName": "张三",
     *     "approveStatus": 1,
     *     "approveTime": "2024-01-01 10:00:00",
     *     "comment": "同意申请"
     *   }
     * ]
     * </p>
     */
    private String approverDetail;

    /**
     * 不同意类型
     * <p>
     * 1=驳回任务<br>
     * 2=返回上一节点
     * </p>
     */
    private Integer rejectType;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
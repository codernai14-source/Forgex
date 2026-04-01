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
 * 审批任务执行实体。
 * <p>
 * 映射表：{@code wf_task_execution}。用于记录审批任务的执行情况。
 * </p>
 *
 * <p>执行状态：</p>
 * <ul>
 *   <li>0=未审批</li>
 *   <li>1=审批中</li>
 *   <li>2=审批完成</li>
 *   <li>3=驳回</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
@TableName("wf_task_execution")
public class WfTaskExecution {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批任务配置表ID
     */
    private Long taskConfigId;

    /**
     * 审批任务编码
     */
    private String taskCode;

    /**
     * 审批任务名称
     */
    private String taskName;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 发起人名称
     */
    private String initiatorName;

    /**
     * 当前节点ID
     */
    private Long currentNodeId;

    /**
     * 当前节点名称
     */
    private String currentNodeName;

    /**
     * 表单实际内容（JSON）
     */
    private String formContent;

    /**
     * 发起时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 状态
     * <p>
     * 0=未审批<br>
     * 1=审批中<br>
     * 2=审批完成<br>
     * 3=驳回
     * </p>
     */
    private Integer status;

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

    /**
     * 逻辑删除：0=未删除，1=已删除
     */
    @TableField("deleted")
    private Integer deleted;
}
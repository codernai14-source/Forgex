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
package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 审批执行数据传输对象。
 * <p>
 * 聚合执行单基础信息、当前审批实例、动作日志以及治理摘要字段，
 * 供待办、已办、我发起、仪表盘和补偿中心统一复用。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
public class WfExecutionDTO {

    /**
     * 执行单 ID。
     */
    private Long id;

    /**
     * 任务配置 ID。
     */
    private Long taskConfigId;

    /**
     * 任务编码。
     */
    private String taskCode;

    /**
     * 任务名称。
     */
    private String taskName;

    /**
     * 发起人 ID。
     */
    private Long initiatorId;

    /**
     * 发起人名称。
     */
    private String initiatorName;

    /**
     * 当前节点 ID。
     */
    private Long currentNodeId;

    /**
     * 当前节点名称。
     */
    private String currentNodeName;

    /**
     * 表单内容。
     */
    private String formContent;

    /**
     * 发起审批时关联任务配置的表单类型。
     */
    private Integer formType;

    /**
     * 发起审批时关联任务配置的自定义表单路径。
     */
    private String formPath;

    /**
     * 发起审批时关联任务配置的低代码表单结构。
     */
    private String taskFormContent;

    /**
     * 发起时间。
     */
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    private LocalDateTime endTime;

    /**
     * 执行状态。
     */
    private Integer status;

    /**
     * 租户 ID。
     */
    private Long tenantId;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 当前审批实例列表。
     */
    private List<WfApprovalInstanceDTO> currentApprovalInstances = new ArrayList<>();

    /**
     * 审批动作日志列表。
     */
    private List<WfApprovalActionLogDTO> approvalActionLogs = new ArrayList<>();

    /**
     * 当前激活实例数量。
     */
    private Integer activeInstanceCount;

    /**
     * 是否存在超时标记。
     */
    private Boolean timeoutFlag;

    /**
     * 是否命中过委托链路。
     */
    private Boolean delegated;

    /**
     * 是否命中过转交链路。
     */
    private Boolean transferred;

    /**
     * 最近一次动作摘要。
     */
    private String latestActionSummary;
}

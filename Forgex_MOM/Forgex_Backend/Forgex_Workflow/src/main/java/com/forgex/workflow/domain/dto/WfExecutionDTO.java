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

/**
 * 审批执行 DTO。
 * <p>
 * 用于传输审批执行数据。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
public class WfExecutionDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 审批任务配置表 ID
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
     * 发起人 ID
     */
    private Long initiatorId;

    /**
     * 发起人名称
     */
    private String initiatorName;

    /**
     * 当前节点 ID
     */
    private Long currentNodeId;

    /**
     * 当前节点名称
     */
    private String currentNodeName;

    /**
     * 表单内容（JSON）
     */
    private String formContent;

    /**
     * 发起时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
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
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
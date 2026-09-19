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
package com.forgex.workflow.service.interpreter;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批上下文。
 * <p>
 * 用于在审批解释器中传递审批流程的上下文信息。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
public class ApprovalContext {
    
    /**
     * 审批任务执行 ID
     */
    private Long executionId;
    
    /**
     * 任务配置 ID
     */
    private Long taskConfigId;
    
    /**
     * 任务编码
     */
    private String taskCode;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 当前节点 ID
     */
    private Long currentNodeId;
    
    /**
     * 当前节点名称
     */
    private String currentNodeName;
    
    /**
     * 上一节点 ID
     */
    private Long previousNodeId;
    
    /**
     * 上一节点名称
     */
    private String previousNodeName;
    
    /**
     * 发起人 ID
     */
    private Long initiatorId;
    
    /**
     * 发起人名称
     */
    private String initiatorName;
    
    /**
     * 表单内容（JSON）
     */
    private String formContent;
    
    /**
     * 任务状态
     * <p>
     * 0=未审批<br>
     * 1=审批中<br>
     * 2=审批完成<br>
     * 3=驳回
     * </p>
     */
    private Integer status;
    
    /**
     * 审批人 ID
     */
    private Long approverId;
    
    /**
     * 审批人名称
     */
    private String approverName;
    
    /**
     * 审批状态
     * <p>
     * 1=同意<br>
     * 2=不同意
     * </p>
     */
    private Integer approveStatus;
    
    /**
     * 审批意见
     */
    private String comment;
    
    /**
     * 租户 ID
     */
    private Long tenantId;
    
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;
}
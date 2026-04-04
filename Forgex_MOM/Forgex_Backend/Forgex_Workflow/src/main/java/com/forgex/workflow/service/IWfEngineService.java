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
package com.forgex.workflow.service;

/**
 * 审批流程引擎服务接口。
 * <p>
 * 提供审批流程的核心引擎功能，包括节点流转、审批人确定、分支判断等。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
public interface IWfEngineService {
    
    /**
     * 初始化审批流程
     * <p>
     * 根据任务配置初始化第一个审批节点
     * </p>
     * 
     * @param executionId 执行 ID
     * @param taskConfigId 任务配置 ID
     */
    void initExecution(Long executionId, Long taskConfigId);
    
    /**
     * 流转到下一节点
     * <p>
     * 根据当前节点和审批结果，流转到下一节点或结束流程
     * </p>
     * 
     * @param executionId 执行 ID
     * @param currentNodeId 当前节点 ID
     * @param approveStatus 审批状态：1=同意，2=不同意
     * @param rejectType 驳回类型：1=驳回任务，2=返回上一节点
     * @return 是否还有下一节点
     */
    Boolean moveToNextNode(Long executionId, Long currentNodeId, Integer approveStatus, Integer rejectType);
    
    /**
     * 确定节点审批人
     * <p>
     * 根据节点配置确定该节点的审批人列表
     * </p>
     * 
     * @param nodeId 节点 ID
     * @param tenantId 租户 ID
     * @return 审批人 ID 列表
     */
    Long[] determineApprovers(Long nodeId, Long tenantId);
    
    /**
     * 评估分支条件
     * <p>
     * 根据表单内容评估分支条件，返回下一节点 ID
     * </p>
     * 
     * @param nodeId 当前节点 ID
     * @param formContent 表单内容（JSON）
     * @return 下一节点 ID，如果没有匹配的条件则返回默认节点 ID
     */
    Long evaluateBranchConditions(Long nodeId, String formContent);
    
    /**
     * 检查节点是否已完成
     * <p>
     * 根据节点的审批类型检查是否所有审批人都已审批
     * </p>
     * 
     * @param executionId 执行 ID
     * @param nodeId 节点 ID
     * @return true=已完成，false=未完成
     */
    Boolean isNodeCompleted(Long executionId, Long nodeId);
    
    /**
     * 结束审批流程
     * 
     * @param executionId 执行 ID
     * @param status 最终状态：2=审批完成，3=驳回
     */
    void finishExecution(Long executionId, Integer status);
}
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

import java.util.List;

/**
 * 审批流程引擎服务接口。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
public interface IWfEngineService {

    /**
     * 初始化审批流程实例。
     * <p>
     * 根据任务配置初始化流程实例，创建第一个节点和执行明细，触发流程启动回调。
     * </p>
     *
     * @param executionId 执行记录 ID
     * @param taskConfigId 任务配置 ID
     * @param selectedApprovers 发起人选择的审批人列表（仅发起人自选模式使用）
     * @throws I18nBusinessException 当任务配置或执行记录不存在时抛出业务异常
     * @see WfTaskExecution
     * @see WfTaskConfig
     */
    void initExecution(Long executionId, Long taskConfigId, List<Long> selectedApprovers);

    /**
     * 流转到下一个节点。
     * <p>
     * 根据审批结果（通过/驳回）流转至下一个节点或结束流程。
     * </p>
     *
     * @param executionId 执行记录 ID
     * @param currentNodeId 当前节点 ID
     * @param approveStatus 审批状态：1-通过，2-驳回
     * @param rejectType 驳回类型：1-驳回结束，2-退回上一步（仅驳回时有效）
     * @return 流转结果，成功返回 true
     * @throws I18nBusinessException 当执行记录或节点不存在时抛出业务异常
     * @see WfTaskExecution
     * @see WfTaskNodeConfig
     */
    Boolean moveToNextNode(Long executionId, Long currentNodeId, Integer approveStatus, Integer rejectType);

    /**
     * 确定节点审批人。
     * <p>
     * 根据节点 ID 和租户 ID 查询配置的审批人，解析审批人类型并返回审批人 ID 数组。
     * </p>
     *
     * @param nodeId 节点 ID
     * @param tenantId 租户 ID
     * @return 审批人 ID 数组
     * @throws I18nBusinessException 当查询失败时抛出业务异常
     * @see WfTaskNodeApprover
     */
    Long[] determineApprovers(Long nodeId, Long tenantId);

    /**
     * 评估分支条件。
     * <p>
     * 根据分支节点的配置和表单内容，评估条件表达式并返回命中的下一节点 ID。
     * </p>
     *
     * @param nodeId 分支节点 ID
     * @param formContent 表单内容（JSON 格式）
     * @return 命中的下一节点 ID，如果没有命中则返回默认节点 ID
     * @throws I18nBusinessException 当节点不存在或条件评估失败时抛出业务异常
     * @see WfTaskNodeConfig
     */
    Long evaluateBranchConditions(Long nodeId, String formContent);

    /**
     * 判断节点是否完成。
     * <p>
     * 根据节点的审批类型和审批实例/审批人记录，判断节点是否已完成审批。
     * </p>
     *
     * @param executionId 执行记录 ID
     * @param nodeId 节点 ID
     * @return 节点是否完成
     * @throws I18nBusinessException 当执行记录或节点不存在时抛出业务异常
     * @see WfTaskApprovalInstance
     */
    Boolean isNodeCompleted(Long executionId, Long nodeId);

    /**
     * 结束审批流程。
     * <p>
     * 更新执行记录状态为结束，触发流程结束回调和通知。
     * </p>
     *
     * @param executionId 执行记录 ID
     * @param status 最终状态：2-审批完成，3-已撤销
     * @throws I18nBusinessException 当执行记录不存在时抛出业务异常
     * @see WfTaskExecution
     */
    void finishExecution(Long executionId, Integer status);
}

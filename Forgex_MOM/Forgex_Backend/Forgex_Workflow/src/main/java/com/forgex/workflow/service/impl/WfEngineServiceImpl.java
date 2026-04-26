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
package com.forgex.workflow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.StatusCode;
import com.forgex.workflow.common.WorkflowConstants;
import com.forgex.common.web.R;
import com.forgex.workflow.client.SysUserClient;
import com.forgex.workflow.domain.entity.WfMyTask;
import com.forgex.workflow.domain.entity.WfTaskApprovalActionLog;
import com.forgex.workflow.domain.entity.WfTaskApprovalInstance;
import com.forgex.workflow.domain.entity.WfTaskConfig;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.domain.entity.WfTaskExecutionApprover;
import com.forgex.workflow.domain.entity.WfTaskExecutionDetail;
import com.forgex.workflow.domain.entity.WfTaskNodeApprover;
import com.forgex.workflow.domain.entity.WfTaskNodeConfig;
import com.forgex.workflow.domain.entity.WfTaskNodeRule;
import com.forgex.workflow.domain.dto.WfNodeApproverDTO;
import com.forgex.workflow.domain.dto.WfTaskNodeRuleDTO;
import com.forgex.workflow.enums.WorkflowPromptEnum;
import com.forgex.workflow.mapper.WfTaskApprovalActionLogMapper;
import com.forgex.workflow.mapper.WfTaskApprovalInstanceMapper;
import com.forgex.workflow.mapper.WfMyTaskMapper;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.mapper.WfTaskExecutionApproverMapper;
import com.forgex.workflow.mapper.WfTaskExecutionDetailMapper;
import com.forgex.workflow.mapper.WfTaskExecutionMapper;
import com.forgex.workflow.mapper.WfTaskNodeApproverMapper;
import com.forgex.workflow.mapper.WfTaskNodeConfigMapper;
import com.forgex.workflow.mapper.WfTaskNodeRuleMapper;
import com.forgex.workflow.service.IWfCallbackService;
import com.forgex.workflow.service.IWfEngineService;
import com.forgex.workflow.service.WorkflowNotificationService;
import com.forgex.workflow.service.interpreter.ApprovalContext;
import com.forgex.workflow.service.interpreter.ApprovalInterpreterRegistry;
import com.forgex.workflow.service.interpreter.IApprovalInterpreter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审批流程引擎服务实现类。
 * <p>
 * 实现 {@link IWfEngineService} 接口，提供审批流程引擎的核心业务逻辑，
 * 包括流程初始化、节点流转、审批人确定、条件分支评估、流程结束等功能。
 * </p>
 * <p>
 * 主要职责：
 * </p>
 * <ul>
 *   <li>流程实例的初始化和启动</li>
 *   <li>审批节点的激活和流转</li>
 *   <li>审批人的确定和解析</li>
 *   <li>分支条件的评估和执行</li>
 *   <li>流程实例的完成和结束</li>
 * </ul>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-04-01
 * @see IWfEngineService
 * @see WfTaskExecution
 * @see WfTaskNodeConfig
 */
@Slf4j
@Service
@DS("workflow")
@RequiredArgsConstructor
public class WfEngineServiceImpl implements IWfEngineService {

    private static final ThreadLocal<List<Long>> START_SELECTED_APPROVERS = new ThreadLocal<>();

    private final WfTaskConfigMapper taskConfigMapper;
    private final WfTaskNodeConfigMapper nodeConfigMapper;
    private final WfTaskNodeApproverMapper nodeApproverMapper;
    private final WfTaskExecutionMapper executionMapper;
    private final WfTaskExecutionDetailMapper executionDetailMapper;
    private final WfTaskExecutionApproverMapper executionApproverMapper;
    private final WfMyTaskMapper myTaskMapper;
    private final WfTaskApprovalInstanceMapper approvalInstanceMapper;
    private final WfTaskApprovalActionLogMapper approvalActionLogMapper;
    private final WfTaskNodeRuleMapper nodeRuleMapper;
    private final ApprovalInterpreterRegistry interpreterRegistry;
    private final SysUserClient sysUserClient;
    private final IWfCallbackService callbackService;
    private final WorkflowNotificationService workflowNotificationService;

    /**
     * 初始化审批流程实例。
     * <p>
     * 根据任务配置初始化流程实例，创建第一个节点和执行明细，触发流程启动回调。
     * </p>
     * <p>
     * 主要步骤：
     * </p>
     * <ol>
     *   <li>校验任务配置和执行记录是否存在</li>
     *   <li>查找第一个审批节点</li>
     *   <li>更新执行记录的当前节点</li>
     *   <li>创建执行明细</li>
     *   <li>触发流程启动解释器</li>
     *   <li>激活起始节点</li>
     * </ol>
     *
     * @param executionId 执行记录 ID
     * @param taskConfigId 任务配置 ID
     * @param selectedApprovers 发起人选择的审批人列表（仅发起人自选模式使用）
     * @throws BusinessException 当任务配置或执行记录不存在时抛出业务异常
     * @see WfTaskExecution
     * @see WfTaskConfig
     * @see WfTaskNodeConfig
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initExecution(Long executionId, Long taskConfigId, List<Long> selectedApprovers) {
        START_SELECTED_APPROVERS.set(selectedApprovers == null ? Collections.emptyList() : new ArrayList<>(selectedApprovers));
        try {
            WfTaskConfig taskConfig = taskConfigMapper.selectById(taskConfigId);
            if (taskConfig == null) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_TASK_CONFIG_NOT_FOUND_FOR_INIT);
            }

            WfTaskExecution execution = executionMapper.selectById(executionId);
            if (execution == null) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_EXECUTION_NOT_FOUND_FOR_INIT);
            }

            WfTaskNodeConfig startNode = findFirstNode(taskConfigId, 1);
            if (startNode == null) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_START_NODE_NOT_CONFIGURED);
            }

            updateExecutionCurrentNode(execution, startNode);
            createExecutionDetail(executionId, startNode, 1, taskConfig.getTenantId());

            callInterpreter(taskConfig.getInterpreterBean(), (interpreter, context) -> {
                populateContext(context, execution, taskConfig);
                context.setCurrentNodeId(startNode.getId());
                context.setCurrentNodeName(startNode.getNodeName());
                interpreter.onStart(context);
            });

            activateFromNode(execution, startNode, taskConfig);

            log.info("初始化审批流程成功，executionId={}, taskCode={}, startNode={}",
                    executionId, taskConfig.getTaskCode(), startNode.getNodeName());
        } finally {
            START_SELECTED_APPROVERS.remove();
        }
    }

    /**
     * 流转到下一个节点。
     * <p>
     * 根据审批结果（通过/驳回）流转至下一个节点或结束流程。
     * </p>
     * <p>
     * 处理逻辑：
     * </p>
     * <ul>
     *   <li>审批通过：激活下一个节点</li>
     *   <li>审批驳回且驳回类型为"退回上一步"：返回上一个审批节点</li>
     *   <li>审批驳回且驳回类型为"驳回结束"：结束流程</li>
     * </ul>
     *
     * @param executionId 执行记录 ID
     * @param currentNodeId 当前节点 ID
     * @param approveStatus 审批状态：1-通过，2-驳回
     * @param rejectType 驳回类型：1-驳回结束，2-退回上一步
     * @return 流转结果，成功返回 true
     * @throws BusinessException 当执行记录或节点不存在时抛出业务异常
     * @see WfTaskExecution
     * @see WfTaskNodeConfig
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean moveToNextNode(Long executionId, Long currentNodeId, Integer approveStatus, Integer rejectType) {
        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_EXECUTION_NOT_FOUND_FOR_INIT);
        }

        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
        WfTaskNodeConfig currentNode = nodeConfigMapper.selectById(currentNodeId);
        if (currentNode == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_NODE_NOT_FOUND);
        }

        // 审批驳回处理
        if (Objects.equals(approveStatus, 2)) {
            if (Objects.equals(rejectType, 2)) {
                // 退回上一步
                WfTaskNodeConfig previousApprovalNode = findPreviousApprovalNode(currentNode);
                if (previousApprovalNode != null) {
                    return activateNode(execution, previousApprovalNode, taskConfig);
                }
            }
            // 驳回结束
            finishExecution(executionId, 3);
            return false;
        }

        // 审批通过，流转至下一个节点
        return activateFromNode(execution, currentNode, taskConfig);
    }

    /**
     * 确定节点审批人。
     * <p>
     * 根据节点 ID 和租户 ID 查询配置的审批人，解析审批人类型并返回审批人 ID 数组。
     * </p>
     *
     * @param nodeId 节点 ID
     * @param tenantId 租户 ID
     * @return 审批人 ID 数组
     * @throws BusinessException 当查询失败时抛出业务异常
     * @see WfTaskNodeApprover
     */
    @Override
    public Long[] determineApprovers(Long nodeId, Long tenantId) {
        List<WfTaskNodeApprover> approvers = nodeApproverMapper.selectList(new LambdaQueryWrapper<WfTaskNodeApprover>()
                .eq(WfTaskNodeApprover::getNodeConfigId, nodeId)
                .eq(WfTaskNodeApprover::getTenantId, tenantId)
                .eq(WfTaskNodeApprover::getDeleted, 0));

        if (approvers == null || approvers.isEmpty()) {
            return new Long[0];
        }

        Set<Long> approverIds = new HashSet<>();
        for (WfTaskNodeApprover approver : approvers) {
            approverIds.addAll(parseApproverIds(approver.getApproverType(), approver.getApproverIds(), tenantId));
        }
        return approverIds.toArray(new Long[0]);
    }

    /**
     * 评估分支条件。
     * <p>
     * 根据分支节点的配置和表单内容，评估条件表达式并返回命中的下一节点 ID。
     * </p>
     * <p>
     * 评估逻辑：
     * </p>
     * <ol>
     *   <li>解析分支条件配置</li>
     *   <li>遍历条件列表</li>
     *   <li>匹配表单字段值</li>
     *   <li>返回第一个命中的下一节点 ID</li>
     *   <li>如果没有命中则返回默认节点 ID</li>
     * </ol>
     *
     * @param nodeId 分支节点 ID
     * @param formContent 表单内容（JSON 格式）
     * @return 命中的下一节点 ID，如果没有命中则返回默认节点 ID
     * @throws BusinessException 当节点不存在或条件评估失败时抛出业务异常
     * @see WfTaskNodeConfig
     */
    @Override
    public Long evaluateBranchConditions(Long nodeId, String formContent) {
        WfTaskNodeConfig node = nodeConfigMapper.selectById(nodeId);
        if (node == null || !Objects.equals(node.getNodeType(), 5)) {
            return null;
        }

        if (!StringUtils.hasText(node.getBranchConditions())) {
            return null;
        }

        JSONObject conditionsObj = JSON.parseObject(node.getBranchConditions());
        JSONArray conditions = conditionsObj.getJSONArray("conditions");
        if (conditions == null || conditions.isEmpty()) {
            return conditionsObj.getLong("defaultNodeId");
        }

        JSONObject formData = StringUtils.hasText(formContent) ? JSON.parseObject(formContent) : new JSONObject();
        for (int i = 0; i < conditions.size(); i++) {
            JSONObject condition = conditions.getJSONObject(i);
            if (condition == null) {
                continue;
            }

            if (evaluateCondition(
                    formData,
                    condition.getString("field"),
                    condition.getString("operator"),
                    condition.getString("value"))) {
                return condition.getLong("nextNodeId");
            }
        }

        return conditionsObj.getLong("defaultNodeId");
    }

    /**
     * 判断节点是否完成。
     * <p>
     * 根据节点的审批类型和审批实例/审批人记录，判断节点是否已完成审批。
     * </p>
     * <p>
     * 完成标准：
     * </p>
     * <ul>
     *   <li>会签（1/5）：所有审批人都已审批</li>
     *   <li>或签（2）：任意一个审批人审批</li>
     *   <li>抄送（3）：无需审批，直接完成</li>
     *   <li>票签（4）：超过半数审批人审批</li>
     * </ul>
     *
     * @param executionId 执行记录 ID
     * @param nodeId 节点 ID
     * @return 节点是否完成
     * @throws BusinessException 当执行记录或节点不存在时抛出业务异常
     * @see WfTaskApprovalInstance
     * @see WfTaskExecutionApprover
     */
    @Override
    public Boolean isNodeCompleted(Long executionId, Long nodeId) {
        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            return false;
        }

        WfTaskExecutionDetail currentDetail = getLatestExecutionDetail(executionId, nodeId);
        List<WfTaskApprovalInstance> instances = listCurrentInstances(executionId, nodeId,
                currentDetail == null ? null : currentDetail.getId());
        if (!instances.isEmpty()) {
            return isNodeCompletedByInstances(nodeId, instances);
        }

        Long tenantId = execution.getTenantId() != null ? execution.getTenantId() : getCurrentTenantId();
        Long[] approvers = determineApprovers(nodeId, tenantId);
        if (approvers.length == 0) {
            return false;
        }
        LambdaQueryWrapper<WfTaskExecutionApprover> wrapper = new LambdaQueryWrapper<WfTaskExecutionApprover>()
                .eq(WfTaskExecutionApprover::getExecutionId, executionId)
                .eq(WfTaskExecutionApprover::getNodeId, nodeId);
        if (currentDetail != null) {
            wrapper.eq(WfTaskExecutionApprover::getExecutionDetailId, currentDetail.getId());
        }

        List<WfTaskExecutionApprover> approverRecords = executionApproverMapper.selectList(wrapper);
        WfTaskNodeConfig node = nodeConfigMapper.selectById(nodeId);
        if (node == null || node.getApproveType() == null) {
            return false;
        }

        Set<Long> approvedUsers = new HashSet<>();
        for (WfTaskExecutionApprover approverRecord : approverRecords) {
            JSONArray approverDetails = StringUtils.hasText(approverRecord.getApproverDetail())
                    ? JSON.parseArray(approverRecord.getApproverDetail())
                    : new JSONArray();
            for (int i = 0; i < approverDetails.size(); i++) {
                JSONObject approverDetail = approverDetails.getJSONObject(i);
                if (approverDetail != null
                        && Objects.equals(approverDetail.getInteger("approveStatus"), 1)
                        && approverDetail.getLong("approverId") != null) {
                    approvedUsers.add(approverDetail.getLong("approverId"));
                }
            }
        }

        long approvedCount = approvedUsers.size();
        switch (node.getApproveType()) {
            case 1:
            case 5:
                // 会签：所有人都需要审批
                return approvedCount >= approvers.length;
            case 2:
                // 或签：任意一人审批即可
                return approvedCount > 0;
            case 3:
                // 抄送：无需审批
                return true;
            case 4:
                // 票签：超过半数
                return approvedCount > approvers.length / 2;
            default:
                return false;
        }
    }

    /**
     * 结束审批流程。
     * <p>
     * 更新执行记录状态为结束，触发流程结束回调和通知。
     * </p>
     * <p>
     * 主要步骤：
     * </p>
     * <ol>
     *   <li>更新执行记录状态和结束时间</li>
     *   <li>关闭待办任务</li>
     *   <li>触发流程结束解释器</li>
     *   <li>触发回调服务</li>
     *   <li>发送完成通知</li>
     * </ol>
     *
     * @param executionId 执行记录 ID
     * @param status 最终状态：2-审批完成，3-已撤销
     * @throws BusinessException 当执行记录不存在时抛出业务异常
     * @see WfTaskExecution
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishExecution(Long executionId, Integer status) {
        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            return;
        }

        // 更新执行记录状态
        execution.setStatus(status);
        execution.setEndTime(LocalDateTime.now());
        executionMapper.updateById(execution);

        // 关闭待办任务
        closePendingMyTasks(executionId, null);

        // 触发流程结束解释器
        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
        callInterpreter(taskConfig == null ? null : taskConfig.getInterpreterBean(), (interpreter, context) -> {
            populateContext(context, execution, taskConfig);
            context.setStatus(status);
            interpreter.onEnd(context);
        });

        // 触发回调和通知
        callbackService.triggerCallback(executionId, status);
        workflowNotificationService.notifyFinished(execution, status);
        log.info("审批流程结束，executionId={}, status={}", executionId, status);
    }

    private Boolean activateFromNode(WfTaskExecution execution, WfTaskNodeConfig currentNode, WfTaskConfig taskConfig) {
        Long nextNodeId = findNextNode(execution.getId(), currentNode, taskConfig);
        if (nextNodeId == null) {
            finishExecution(execution.getId(), 2);
            return false;
        }

        WfTaskNodeConfig nextNode = nodeConfigMapper.selectById(nextNodeId);
        if (nextNode == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_NEXT_NODE_NOT_FOUND);
        }
        return activateNode(execution, nextNode, taskConfig);
    }

    private Boolean activateNode(WfTaskExecution execution, WfTaskNodeConfig node, WfTaskConfig taskConfig) {
        updateExecutionCurrentNode(execution, node);

        if (Objects.equals(node.getNodeType(), 2)) {
            createExecutionDetail(execution.getId(), node, 1, execution.getTenantId());
            finishExecution(execution.getId(), 2);
            return false;
        }

        if (Objects.equals(node.getNodeType(), 3)) {
            return activateApprovalNode(execution, node, taskConfig);
        }

        createExecutionDetail(execution.getId(), node, 1, execution.getTenantId());
        return activateFromNode(execution, node, taskConfig);
    }

    private Boolean activateApprovalNode(WfTaskExecution execution, WfTaskNodeConfig node, WfTaskConfig taskConfig) {
        WfTaskExecutionDetail detail = createExecutionDetail(execution.getId(), node, 0, execution.getTenantId());
        List<WfTaskApprovalInstance> instances = createApprovalInstances(execution, node, detail);
        Long[] approvers = instances.stream()
                .filter(instance -> Boolean.TRUE.equals(instance.getActivated()))
                .map(WfTaskApprovalInstance::getApproverId)
                .filter(Objects::nonNull)
                .distinct()
                .toArray(Long[]::new);
        if (approvers.length == 0) {
            log.warn("审批节点未匹配到有效审批人，阻止自动流转，executionId={}, nodeId={}", execution.getId(), node.getId());
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_NODE_APPROVER_RESOLVE_EMPTY);
        }

        workflowNotificationService.notifyPendingApprovers(execution, node, approvers);
        return true;
    }

    private WfTaskExecutionDetail createExecutionDetail(Long executionId,
                                                        WfTaskNodeConfig node,
                                                        Integer currentStatus,
                                                        Long tenantId) {
        WfTaskExecutionDetail detail = new WfTaskExecutionDetail();
        detail.setExecutionId(executionId);
        detail.setNodeId(node.getId());
        detail.setNodeName(node.getNodeName());
        detail.setCurrentStatus(currentStatus);
        detail.setTenantId(tenantId);
        executionDetailMapper.insert(detail);
        return detail;
    }

    private void createMyTask(WfTaskExecution execution, WfTaskNodeConfig node, Long[] approverIds) {
        closePendingMyTasks(execution.getId(), node.getId());

        WfMyTask myTask = new WfMyTask();
        myTask.setExecutionId(execution.getId());
        myTask.setTaskName(execution.getTaskName());
        myTask.setNodeId(node.getId());
        myTask.setNodeName(node.getNodeName());
        myTask.setApproverIds(JSON.toJSONString(Arrays.asList(approverIds)));
        myTask.setStatus(0);
        myTask.setTenantId(execution.getTenantId());
        myTaskMapper.insert(myTask);
    }
    private void createInstanceMyTask(WfTaskExecution execution,
                                      WfTaskNodeConfig node,
                                      WfTaskExecutionDetail detail,
                                      WfTaskApprovalInstance instance) {
        WfMyTask myTask = new WfMyTask();
        myTask.setExecutionId(execution.getId());
        myTask.setTaskName(execution.getTaskName());
        myTask.setNodeId(node.getId());
        myTask.setNodeName(node.getNodeName());
        myTask.setApprovalInstanceId(instance.getId());
        myTask.setExecutionDetailId(detail.getId());
        myTask.setApproverId(instance.getApproverId());
        myTask.setApproverIds(JSON.toJSONString(Collections.singletonList(instance.getApproverId())));
        myTask.setStatus(0);
        myTask.setTenantId(execution.getTenantId());
        myTask.setDeadlineTime(instance.getDeadlineTime());
        myTaskMapper.insert(myTask);
    }

    private void closePendingMyTasks(Long executionId, Long nodeId) {
        LambdaQueryWrapper<WfMyTask> wrapper = new LambdaQueryWrapper<WfMyTask>()
                .eq(WfMyTask::getExecutionId, executionId)
                .eq(WfMyTask::getStatus, 0);
        if (nodeId != null) {
            wrapper.eq(WfMyTask::getNodeId, nodeId);
        }

        List<WfMyTask> pendingTasks = myTaskMapper.selectList(wrapper);
        for (WfMyTask pendingTask : pendingTasks) {
            pendingTask.setStatus(1);
            myTaskMapper.updateById(pendingTask);
        }
    }

    private WfTaskNodeConfig findFirstNode(Long taskConfigId, Integer nodeType) {
        return nodeConfigMapper.selectOne(new LambdaQueryWrapper<WfTaskNodeConfig>()
                .eq(WfTaskNodeConfig::getTaskConfigId, taskConfigId)
                .eq(WfTaskNodeConfig::getNodeType, nodeType)
                .eq(WfTaskNodeConfig::getDeleted, 0)
                .orderByAsc(WfTaskNodeConfig::getOrderNum)
                .last("LIMIT 1"));
    }

    private WfTaskNodeConfig findPreviousApprovalNode(WfTaskNodeConfig node) {
        List<Long> previousNodeIds = parseNodeIds(node.getPreNodeIds());
        for (Long previousNodeId : previousNodeIds) {
            WfTaskNodeConfig previousNode = nodeConfigMapper.selectById(previousNodeId);
            if (previousNode == null) {
                continue;
            }
            if (Objects.equals(previousNode.getNodeType(), 3)) {
                return previousNode;
            }

            WfTaskNodeConfig nestedPreviousNode = findPreviousApprovalNode(previousNode);
            if (nestedPreviousNode != null) {
                return nestedPreviousNode;
            }
        }
        return null;
    }

    private WfTaskExecutionDetail getLatestExecutionDetail(Long executionId, Long nodeId) {
        return executionDetailMapper.selectOne(new LambdaQueryWrapper<WfTaskExecutionDetail>()
                .eq(WfTaskExecutionDetail::getExecutionId, executionId)
                .eq(WfTaskExecutionDetail::getNodeId, nodeId)
                .orderByDesc(WfTaskExecutionDetail::getId)
                .last("LIMIT 1"));
    }

    private void updateExecutionCurrentNode(WfTaskExecution execution, WfTaskNodeConfig node) {
        execution.setCurrentNodeId(node.getId());
        execution.setCurrentNodeName(node.getNodeName());
        executionMapper.updateById(execution);
    }

    private List<Long> parseApproverIds(Integer approverType, String approverIds, Long tenantId) {
        if (!StringUtils.hasText(approverIds)) {
            return Collections.emptyList();
        }

        JSONArray array = JSON.parseArray(approverIds);
        List<Long> idList = array.stream()
                .map(item -> Long.valueOf(item.toString()))
                .collect(Collectors.toList());

        switch (approverType) {
            case 1:
                return idList;
            case 2:
                return getUserIdsByDeptIds(idList);
            case 3:
                return getUserIdsByRoleIds(idList);
            case 4:
                return getUserIdsByPositionIds(idList);
            case 5:
            case 6:
                return idList;
            default:
                return Collections.emptyList();
        }
    }

    private List<Long> getUserIdsByDeptIds(List<Long> deptIds) {
        try {
            R<List<Long>> result = sysUserClient.listUserIdsByDeptIds(deptIds);
            if (result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
            log.warn("审批人部门匹配返回异常，tenantId={}, deptIds={}, result={}",
                    getCurrentTenantId(), deptIds, JSON.toJSONString(result));
        } catch (Exception ex) {
            log.error("根据部门查询审批人失败，deptIds={}", deptIds, ex);
        }
        return Collections.emptyList();
    }

    private List<Long> getUserIdsByRoleIds(List<Long> roleIds) {
        try {
            R<List<Long>> result = sysUserClient.listUserIdsByRoleIds(roleIds);
            if (result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
            log.warn("审批人角色匹配返回异常，tenantId={}, roleIds={}, result={}",
                    getCurrentTenantId(), roleIds, JSON.toJSONString(result));
        } catch (Exception ex) {
            log.error("根据角色查询审批人失败，roleIds={}", roleIds, ex);
        }
        return Collections.emptyList();
    }

    private List<Long> getUserIdsByPositionIds(List<Long> positionIds) {
        try {
            R<List<Long>> result = sysUserClient.listUserIdsByPositionIds(positionIds);
            if (result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
            log.warn("审批人岗位匹配返回异常，tenantId={}, positionIds={}, result={}",
                    getCurrentTenantId(), positionIds, JSON.toJSONString(result));
        } catch (Exception ex) {
            log.error("根据职位查询审批人失败，positionIds={}", positionIds, ex);
        }
        return Collections.emptyList();
    }

    private Long findNextNode(Long executionId, WfTaskNodeConfig currentNode, WfTaskConfig taskConfig) {
        if (Objects.equals(currentNode.getNodeType(), 5)) {
            WfTaskExecution execution = executionMapper.selectById(executionId);
            return evaluateBranchConditions(currentNode.getId(), execution == null ? null : execution.getFormContent());
        }

        if (!StringUtils.hasText(currentNode.getNextNodeIds())) {
            return null;
        }

        JSONArray nextNodeIds = JSON.parseArray(currentNode.getNextNodeIds());
        if (nextNodeIds == null || nextNodeIds.isEmpty()) {
            return null;
        }
        return nextNodeIds.getLong(0);
    }

    private List<Long> parseNodeIds(String nodeIdsJson) {
        if (!StringUtils.hasText(nodeIdsJson)) {
            return List.of();
        }
        JSONArray nodeIds = JSON.parseArray(nodeIdsJson);
        if (nodeIds == null || nodeIds.isEmpty()) {
            return List.of();
        }
        return nodeIds.stream()
                .map(item -> Long.valueOf(item.toString()))
                .collect(Collectors.toList());
    }

    private Boolean evaluateCondition(JSONObject formData, String field, String operator, String value) {
        if (formData == null || !StringUtils.hasText(field) || !StringUtils.hasText(operator)) {
            return false;
        }

        Object fieldValue = formData.get(field);
        if (fieldValue == null) {
            return false;
        }

        try {
            switch (operator) {
                case "=":
                    return String.valueOf(fieldValue).equals(value);
                case "!=":
                    return !String.valueOf(fieldValue).equals(value);
                case ">":
                    return Double.parseDouble(String.valueOf(fieldValue)) > Double.parseDouble(value);
                case ">=":
                    return Double.parseDouble(String.valueOf(fieldValue)) >= Double.parseDouble(value);
                case "<":
                    return Double.parseDouble(String.valueOf(fieldValue)) < Double.parseDouble(value);
                case "<=":
                    return Double.parseDouble(String.valueOf(fieldValue)) <= Double.parseDouble(value);
                case "like":
                    return String.valueOf(fieldValue).contains(value);
                case "in":
                    return JSON.parseArray(value).contains(fieldValue);
                default:
                    return false;
            }
        } catch (Exception ex) {
            log.warn("分支条件计算失败，field={}, operator={}, value={}", field, operator, value, ex);
            return false;
        }
    }

    private Long getCurrentTenantId() {
        Long tenantId = CurrentUserUtils.getTenantId();
        return tenantId == null ? 1L : tenantId;
    }

    private void populateContext(ApprovalContext context, WfTaskExecution execution, WfTaskConfig taskConfig) {
        context.setExecutionId(execution.getId());
        context.setTaskConfigId(execution.getTaskConfigId());
        context.setTaskCode(execution.getTaskCode());
        context.setTaskName(execution.getTaskName());
        context.setCurrentNodeId(execution.getCurrentNodeId());
        context.setCurrentNodeName(execution.getCurrentNodeName());
        context.setInitiatorId(execution.getInitiatorId());
        context.setInitiatorName(execution.getInitiatorName());
        context.setFormContent(execution.getFormContent());
        context.setStatus(execution.getStatus());
        context.setTenantId(execution.getTenantId());
        if (taskConfig != null) {
            context.setTaskConfigId(taskConfig.getId());
        }
    }

    private void callInterpreter(String beanName, InterpreterAction action) {
        IApprovalInterpreter interpreter = interpreterRegistry.getInterpreter(beanName);
        if (interpreter == null) {
            interpreter = interpreterRegistry.getInterpreter("emptyApprovalInterpreter");
        }
        if (interpreter != null) {
            action.execute(interpreter, new ApprovalContext());
        }
    }

    @FunctionalInterface
    private interface InterpreterAction {
        void execute(IApprovalInterpreter interpreter, ApprovalContext context);
    }

    private List<WfTaskApprovalInstance> createApprovalInstances(WfTaskExecution execution,
                                                                 WfTaskNodeConfig node,
                                                                 WfTaskExecutionDetail detail) {
        closePendingMyTasks(execution.getId(), node.getId());
        List<WfTaskNodeRuleDTO> rules = loadNodeRuleConfigs(node.getId(), execution.getTenantId());
        List<ResolvedApprover> resolvedApprovers = resolveApproversForNode(node, execution, rules);
        if (resolvedApprovers.isEmpty()) {
            log.warn("审批节点未匹配到有效审批人，tenantId={}, executionId={}, taskCode={}, nodeId={}, nodeName={}, approveType={}, rules={}",
                    execution.getTenantId(),
                    execution.getId(),
                    execution.getTaskCode(),
                    node.getId(),
                    node.getNodeName(),
                    node.getApproveType(),
                    JSON.toJSONString(rules));
            return Collections.emptyList();
        }

        boolean sequential = Objects.equals(node.getApproveType(), WorkflowConstants.ApproveType.SEQUENTIAL);
        List<WfTaskApprovalInstance> instances = new ArrayList<>();
        for (int index = 0; index < resolvedApprovers.size(); index++) {
            ResolvedApprover approver = resolvedApprovers.get(index);
            WfTaskApprovalInstance instance = new WfTaskApprovalInstance();
            instance.setExecutionId(execution.getId());
            instance.setExecutionDetailId(detail.getId());
            instance.setNodeId(node.getId());
            instance.setInstanceNo(execution.getId() + "-" + node.getId() + "-" + (index + 1));
            instance.setApproverId(approver.approverId);
            instance.setApproverName(approver.approverName);
            instance.setApproverSourceType(approver.approverType);
            instance.setSourceRuleId(approver.ruleId);
            instance.setSourceSnapshot(approver.sourceSnapshot);
            instance.setStatus(WorkflowConstants.ApprovalInstanceStatus.PENDING);
            instance.setActivated(!sequential || index == 0);
            instance.setDeadlineTime(resolveDeadlineTime(approver.timeoutHours));
            instance.setTenantId(execution.getTenantId());
            instance.setDeleted(0);
            approvalInstanceMapper.insert(instance);
            instances.add(instance);
            if (Boolean.TRUE.equals(instance.getActivated())) {
                createInstanceMyTask(execution, node, detail, instance);
            }
        }
        return instances;
    }

    private List<WfTaskApprovalInstance> listCurrentInstances(Long executionId, Long nodeId, Long executionDetailId) {
        LambdaQueryWrapper<WfTaskApprovalInstance> wrapper = new LambdaQueryWrapper<WfTaskApprovalInstance>()
                .eq(WfTaskApprovalInstance::getExecutionId, executionId)
                .eq(WfTaskApprovalInstance::getNodeId, nodeId)
                .eq(WfTaskApprovalInstance::getDeleted, 0)
                .orderByAsc(WfTaskApprovalInstance::getId);
        if (executionDetailId != null) {
            wrapper.eq(WfTaskApprovalInstance::getExecutionDetailId, executionDetailId);
        }
        return approvalInstanceMapper.selectList(wrapper);
    }

    private boolean isNodeCompletedByInstances(Long nodeId, List<WfTaskApprovalInstance> instances) {
        WfTaskNodeConfig node = nodeConfigMapper.selectById(nodeId);
        if (node == null || node.getApproveType() == null) {
            return false;
        }
        long approvedCount = instances.stream()
                .filter(item -> Objects.equals(item.getStatus(), WorkflowConstants.ApprovalInstanceStatus.APPROVED))
                .count();
        switch (node.getApproveType()) {
            case 1:
            case 5:
                return approvedCount >= instances.size();
            case 2:
                return approvedCount > 0;
            case 3:
                return true;
            case 4:
                return approvedCount > instances.size() / 2;
            default:
                return false;
        }
    }

    private List<WfTaskNodeRuleDTO> loadNodeRuleConfigs(Long nodeId, Long tenantId) {
        List<WfTaskNodeRule> rules = nodeRuleMapper.selectList(new LambdaQueryWrapper<WfTaskNodeRule>()
                .eq(WfTaskNodeRule::getNodeConfigId, nodeId)
                .eq(WfTaskNodeRule::getTenantId, tenantId)
                .eq(WfTaskNodeRule::getDeleted, 0)
                .orderByAsc(WfTaskNodeRule::getSortOrder)
                .orderByAsc(WfTaskNodeRule::getId));
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        List<WfTaskNodeApprover> approvers = nodeApproverMapper.selectList(new LambdaQueryWrapper<WfTaskNodeApprover>()
                .eq(WfTaskNodeApprover::getNodeConfigId, nodeId)
                .eq(WfTaskNodeApprover::getTenantId, tenantId)
                .eq(WfTaskNodeApprover::getDeleted, 0)
                .orderByAsc(WfTaskNodeApprover::getId));
        List<WfNodeApproverDTO> approverDTOs = approvers.stream().map(item -> {
            WfNodeApproverDTO dto = new WfNodeApproverDTO();
            dto.setApproverType(item.getApproverType());
            dto.setApproverIds(parseJsonApproverIds(item.getApproverIds()));
            return dto;
        }).collect(Collectors.toList());
        return rules.stream().map(rule -> {
            WfTaskNodeRuleDTO dto = new WfTaskNodeRuleDTO();
            dto.setId(rule.getId());
            dto.setRuleName(rule.getRuleName());
            dto.setRuleType(rule.getRuleType());
            dto.setApproveMode(rule.getApproveMode());
            dto.setApprovalThreshold(rule.getApprovalThreshold());
            dto.setSortOrder(rule.getSortOrder());
            dto.setTimeoutHours(rule.getTimeoutHours());
            dto.setTimeoutAction(rule.getTimeoutAction());
            dto.setAllowInitiatorSelect(rule.getAllowInitiatorSelect());
            dto.setSuperiorLevel(rule.getSuperiorLevel());
            dto.setAllowAddSign(rule.getAllowAddSign());
            dto.setAllowTransfer(rule.getAllowTransfer());
            dto.setAllowDelegate(rule.getAllowDelegate());
            dto.setAllowRecall(rule.getAllowRecall());
            dto.setApprovers(new ArrayList<>(approverDTOs));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<ResolvedApprover> resolveApproversForNode(WfTaskNodeConfig node,
                                                           WfTaskExecution execution,
                                                           List<WfTaskNodeRuleDTO> rules) {
        if (rules == null || rules.isEmpty()) {
            return Arrays.stream(determineApprovers(node.getId(), execution.getTenantId()))
                    .map(approverId -> new ResolvedApprover(
                            approverId,
                            "用户" + approverId,
                            WorkflowConstants.ApproverType.SINGLE,
                            null,
                            null,
                            null))
                    .collect(Collectors.toList());
        }
        Set<Long> deduplicated = new LinkedHashSet<>();
        List<ResolvedApprover> resolved = new ArrayList<>();
        boolean hasRuntimeSelectionRule = false;
        for (WfTaskNodeRuleDTO rule : rules) {
            boolean requiresSelectedApprovers = Objects.equals(rule.getRuleType(), WorkflowConstants.RuleType.INITIATOR_SELECTED)
                    || Boolean.TRUE.equals(rule.getAllowInitiatorSelect());
            hasRuntimeSelectionRule = hasRuntimeSelectionRule || requiresSelectedApprovers;
            List<WfNodeApproverDTO> approvers = rule.getApprovers() == null ? Collections.emptyList() : rule.getApprovers();
            if (approvers.isEmpty() && requiresSelectedApprovers) {
                approvers = Collections.singletonList(buildSyntheticApprover(WorkflowConstants.ApproverType.INITIATOR_SELECTED));
            }
            boolean ruleResolved = false;
            for (WfNodeApproverDTO approver : approvers) {
                if (approver == null || approver.getApproverType() == null) {
                    continue;
                }
                for (Long approverId : resolveApproverIdsByRule(approver, execution)) {
                    if (approverId == null || !deduplicated.add(approverId)) {
                        continue;
                    }
                    ruleResolved = true;
                    resolved.add(new ResolvedApprover(
                            approverId,
                            "用户" + approverId,
                            approver.getApproverType(),
                            rule.getId(),
                            JSON.toJSONString(rule),
                            rule.getTimeoutHours()));
                }
            }
            if (!ruleResolved && rule.getFallbackApproverIds() != null) {
                for (Long approverId : rule.getFallbackApproverIds()) {
                    if (approverId == null || !deduplicated.add(approverId)) {
                        continue;
                    }
                    resolved.add(new ResolvedApprover(
                            approverId,
                            "用户" + approverId,
                            WorkflowConstants.ApproverType.SINGLE,
                            rule.getId(),
                            JSON.toJSONString(rule),
                            rule.getTimeoutHours()));
                }
            }
        }
        if (resolved.isEmpty() && !hasRuntimeSelectionRule) {
            return Arrays.stream(determineApprovers(node.getId(), execution.getTenantId()))
                    .map(approverId -> new ResolvedApprover(
                            approverId,
                            "用户" + approverId,
                            WorkflowConstants.ApproverType.SINGLE,
                            null,
                            null,
                            null))
                    .collect(Collectors.toList());
        }
        return resolved;
    }

    private WfNodeApproverDTO buildSyntheticApprover(Integer approverType) {
        WfNodeApproverDTO approver = new WfNodeApproverDTO();
        approver.setApproverType(approverType);
        approver.setApproverIds(Collections.emptyList());
        return approver;
    }

    private List<Long> resolveApproverIdsByRule(WfNodeApproverDTO approver, WfTaskExecution execution) {
        if (approver.getApproverType() == null) {
            return Collections.emptyList();
        }
        if (Objects.equals(approver.getApproverType(), WorkflowConstants.ApproverType.INITIATOR_SELECTED)) {
            List<Long> selectedApprovers = START_SELECTED_APPROVERS.get();
            if (selectedApprovers != null && !selectedApprovers.isEmpty()) {
                return selectedApprovers;
            }
            return approver.getApproverIds() == null ? Collections.emptyList() : approver.getApproverIds();
        }
        return parseApproverIds(approver.getApproverType(),
                JSON.toJSONString(approver.getApproverIds() == null ? Collections.emptyList() : approver.getApproverIds()),
                execution.getTenantId());
    }

    private List<Long> parseJsonApproverIds(String approverIds) {
        if (!StringUtils.hasText(approverIds)) {
            return Collections.emptyList();
        }
        JSONArray array = JSON.parseArray(approverIds);
        if (array == null || array.isEmpty()) {
            return Collections.emptyList();
        }
        return array.stream().map(item -> Long.valueOf(String.valueOf(item))).collect(Collectors.toList());
    }

    private LocalDateTime resolveDeadlineTime(Integer timeoutHours) {
        if (timeoutHours == null || timeoutHours <= 0) {
            return null;
        }
        return LocalDateTime.now().plusHours(timeoutHours);
    }

    private static class ResolvedApprover {
        private final Long approverId;
        private final String approverName;
        private final Integer approverType;
        private final Long ruleId;
        private final String sourceSnapshot;
        private final Integer timeoutHours;

        private ResolvedApprover(Long approverId,
                                 String approverName,
                                 Integer approverType,
                                 Long ruleId,
                                 String sourceSnapshot,
                                 Integer timeoutHours) {
            this.approverId = approverId;
            this.approverName = approverName;
            this.approverType = approverType;
            this.ruleId = ruleId;
            this.sourceSnapshot = sourceSnapshot;
            this.timeoutHours = timeoutHours;
        }
    }
}

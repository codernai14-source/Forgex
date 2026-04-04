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
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.R;
import com.forgex.workflow.client.SysUserClient;
import com.forgex.workflow.domain.entity.WfMyTask;
import com.forgex.workflow.domain.entity.WfTaskConfig;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.domain.entity.WfTaskExecutionApprover;
import com.forgex.workflow.domain.entity.WfTaskExecutionDetail;
import com.forgex.workflow.domain.entity.WfTaskNodeApprover;
import com.forgex.workflow.domain.entity.WfTaskNodeConfig;
import com.forgex.workflow.mapper.WfMyTaskMapper;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.mapper.WfTaskExecutionApproverMapper;
import com.forgex.workflow.mapper.WfTaskExecutionDetailMapper;
import com.forgex.workflow.mapper.WfTaskExecutionMapper;
import com.forgex.workflow.mapper.WfTaskNodeApproverMapper;
import com.forgex.workflow.mapper.WfTaskNodeConfigMapper;
import com.forgex.workflow.service.IWfCallbackService;
import com.forgex.workflow.service.IWfEngineService;
import com.forgex.workflow.service.interpreter.ApprovalContext;
import com.forgex.workflow.service.interpreter.ApprovalInterpreterRegistry;
import com.forgex.workflow.service.interpreter.IApprovalInterpreter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审批流程引擎服务实现。
 */
@Slf4j
@Service
@DS("workflow")
@RequiredArgsConstructor
public class WfEngineServiceImpl implements IWfEngineService {

    private final WfTaskConfigMapper taskConfigMapper;
    private final WfTaskNodeConfigMapper nodeConfigMapper;
    private final WfTaskNodeApproverMapper nodeApproverMapper;
    private final WfTaskExecutionMapper executionMapper;
    private final WfTaskExecutionDetailMapper executionDetailMapper;
    private final WfTaskExecutionApproverMapper executionApproverMapper;
    private final WfMyTaskMapper myTaskMapper;
    private final ApprovalInterpreterRegistry interpreterRegistry;
    private final SysUserClient sysUserClient;
    private final IWfCallbackService callbackService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initExecution(Long executionId, Long taskConfigId) {
        WfTaskConfig taskConfig = taskConfigMapper.selectById(taskConfigId);
        if (taskConfig == null) {
            throw new BusinessException("审批任务配置不存在");
        }

        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            throw new BusinessException("审批执行记录不存在");
        }

        WfTaskNodeConfig startNode = findFirstNode(taskConfigId, 1);
        if (startNode == null) {
            throw new BusinessException("审批任务未配置开始节点");
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean moveToNextNode(Long executionId, Long currentNodeId, Integer approveStatus, Integer rejectType) {
        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            throw new BusinessException("审批执行记录不存在");
        }

        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
        WfTaskNodeConfig currentNode = nodeConfigMapper.selectById(currentNodeId);
        if (currentNode == null) {
            throw new BusinessException("当前节点不存在");
        }

        if (Objects.equals(approveStatus, 2)) {
            if (Objects.equals(rejectType, 2)) {
                WfTaskNodeConfig previousApprovalNode = findPreviousApprovalNode(currentNode);
                if (previousApprovalNode != null) {
                    return activateNode(execution, previousApprovalNode, taskConfig);
                }
            }
            finishExecution(executionId, 3);
            return false;
        }

        return activateFromNode(execution, currentNode, taskConfig);
    }

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

    @Override
    public Boolean isNodeCompleted(Long executionId, Long nodeId) {
        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            return false;
        }

        Long tenantId = execution.getTenantId() != null ? execution.getTenantId() : getCurrentTenantId();
        Long[] approvers = determineApprovers(nodeId, tenantId);
        if (approvers.length == 0) {
            return true;
        }

        WfTaskExecutionDetail currentDetail = getLatestExecutionDetail(executionId, nodeId);
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
                return approvedCount >= approvers.length;
            case 2:
                return approvedCount > 0;
            case 3:
                return true;
            case 4:
                return approvedCount > approvers.length / 2;
            default:
                return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishExecution(Long executionId, Integer status) {
        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            return;
        }

        execution.setStatus(status);
        execution.setEndTime(LocalDateTime.now());
        executionMapper.updateById(execution);

        closePendingMyTasks(executionId, null);

        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
        callInterpreter(taskConfig == null ? null : taskConfig.getInterpreterBean(), (interpreter, context) -> {
            populateContext(context, execution, taskConfig);
            context.setStatus(status);
            interpreter.onEnd(context);
        });

        callbackService.triggerCallback(executionId, status);
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
            throw new BusinessException("下一节点不存在");
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
        Long[] approvers = determineApprovers(node.getId(), execution.getTenantId());
        if (approvers.length == 0) {
            detail.setCurrentStatus(1);
            executionDetailMapper.updateById(detail);
            log.warn("审批节点未配置审批人，自动流转，executionId={}, nodeId={}", execution.getId(), node.getId());
            return activateFromNode(execution, node, taskConfig);
        }

        createMyTask(execution, node, approvers);
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
}

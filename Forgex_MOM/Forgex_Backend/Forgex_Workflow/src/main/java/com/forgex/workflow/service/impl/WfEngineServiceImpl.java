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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.web.R;
import com.forgex.workflow.client.SysUserClient;
import com.forgex.workflow.domain.entity.*;
import com.forgex.workflow.mapper.*;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审批流程引擎服务实现。
 * <p>
 * 提供审批流程的核心引擎功能实现。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Slf4j
@Service
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
        // 获取任务配置
        WfTaskConfig taskConfig = taskConfigMapper.selectById(taskConfigId);
        if (taskConfig == null) {
            throw new BusinessException("审批任务配置不存在");
        }
        
        // 获取第一个节点（开始节点）
        LambdaQueryWrapper<WfTaskNodeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskNodeConfig::getTaskConfigId, taskConfigId)
               .eq(WfTaskNodeConfig::getNodeType, 1) // 开始节点
               .eq(WfTaskNodeConfig::getDeleted, false)
               .orderByAsc(WfTaskNodeConfig::getOrderNum)
               .last("LIMIT 1");
        
        WfTaskNodeConfig startNode = nodeConfigMapper.selectOne(wrapper);
        if (startNode == null) {
            throw new BusinessException("审批任务未配置开始节点");
        }
        
        // 创建执行详情
        WfTaskExecutionDetail detail = new WfTaskExecutionDetail();
        detail.setExecutionId(executionId);
        detail.setNodeId(startNode.getId());
        detail.setNodeName(startNode.getNodeName());
        detail.setCurrentStatus(0); // 未审批
        detail.setTenantId(taskConfig.getTenantId());
        
        executionDetailMapper.insert(detail);
        
        // 更新执行记录的当前节点
        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution != null) {
            execution.setCurrentNodeId(startNode.getId());
            execution.setCurrentNodeName(startNode.getNodeName());
            executionMapper.updateById(execution);
        }
        
        // 调用解释器
        callInterpreter(taskConfig.getInterpreterBean(), (interpreter, context) -> {
            context.setExecutionId(executionId);
            context.setTaskConfigId(taskConfigId);
            context.setTaskCode(taskConfig.getTaskCode());
            context.setTaskName(taskConfig.getTaskName());
            interpreter.onStart(context);
        });
        
        log.info("初始化审批流程成功，executionId={}, taskName={}, startNode={}", 
                executionId, taskConfig.getTaskName(), startNode.getNodeName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean moveToNextNode(Long executionId, Long currentNodeId, Integer approveStatus, Integer rejectType) {
        // 获取执行记录
        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            throw new BusinessException("审批执行记录不存在");
        }
        
        // 获取任务配置
        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
        
        // 获取当前节点
        WfTaskNodeConfig currentNode = nodeConfigMapper.selectById(currentNodeId);
        if (currentNode == null) {
            throw new BusinessException("当前节点不存在");
        }
        
        // 如果是驳回
        if (approveStatus == 2) {
            if (rejectType == 1) {
                // 驳回任务，直接结束
                finishExecution(executionId, 3);
                return false;
            } else if (rejectType == 2) {
                // 返回上一节点
                // TODO: 实现返回上一节点逻辑
                return true;
            }
        }
        
        // 检查是否是结束节点
        if (currentNode.getNodeType() == 2) { // 结束节点
            finishExecution(executionId, 2);
            return false;
        }
        
        // 获取下一节点
        Long nextNodeId = findNextNode(executionId, currentNode, taskConfig);
        
        if (nextNodeId == null) {
            // 没有下一节点，结束流程
            finishExecution(executionId, 2);
            return false;
        }
        
        // 更新执行记录的当前节点
        WfTaskNodeConfig nextNode = nodeConfigMapper.selectById(nextNodeId);
        execution.setCurrentNodeId(nextNodeId);
        execution.setCurrentNodeName(nextNode.getNodeName());
        executionMapper.updateById(execution);
        
        // 创建下一节点的执行详情
        WfTaskExecutionDetail detail = new WfTaskExecutionDetail();
        detail.setExecutionId(executionId);
        detail.setNodeId(nextNodeId);
        detail.setNodeName(nextNode.getNodeName());
        detail.setCurrentStatus(0);
        detail.setTenantId(taskConfig.getTenantId());
        
        executionDetailMapper.insert(detail);
        
        log.info("流转到下一节点，executionId={}, fromNode={}, toNode={}", 
                executionId, currentNode.getNodeName(), nextNode.getNodeName());
        
        return true;
    }
    
    @Override
    public Long[] determineApprovers(Long nodeId, Long tenantId) {
        // 获取节点审批人配置
        LambdaQueryWrapper<WfTaskNodeApprover> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskNodeApprover::getNodeConfigId, nodeId)
               .eq(WfTaskNodeApprover::getTenantId, tenantId)
               .eq(WfTaskNodeApprover::getDeleted, false);
        
        List<WfTaskNodeApprover> approvers = nodeApproverMapper.selectList(wrapper);
        
        if (approvers.isEmpty()) {
            return new Long[0];
        }
        
        // 合并所有审批人（去重）
        Set<Long> approverIds = new HashSet<>();
        
        for (WfTaskNodeApprover approver : approvers) {
            List<Long> ids = parseApproverIds(approver.getApproverType(), 
                                              approver.getApproverIds(), 
                                              tenantId);
            approverIds.addAll(ids);
        }
        
        return approverIds.toArray(new Long[0]);
    }
    
    /**
     * 解析审批人 ID 列表
     * 
     * @param approverType 审批人类型
     * @param approverIds 审批人 ID 集合（JSON 数组）
     * @param tenantId 租户 ID
     * @return 用户 ID 列表
     */
    private List<Long> parseApproverIds(Integer approverType, String approverIds, Long tenantId) {
        if (!StringUtils.hasText(approverIds)) {
            return Collections.emptyList();
        }
        
        JSONArray array = JSON.parseArray(approverIds);
        List<Long> idList = array.stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(Collectors.toList());
        
        switch (approverType) {
            case 1: // 单人
                return idList;
            case 2: // 部门
                return getUserIdsByDeptIds(idList);
            case 3: // 角色
                return getUserIdsByRoleIds(idList);
            case 4: // 职位
                return getUserIdsByPositionIds(idList);
            default:
                return Collections.emptyList();
        }
    }
    
    /**
     * 根据部门 ID 列表获取用户 ID 列表
     */
    private List<Long> getUserIdsByDeptIds(List<Long> deptIds) {
        try {
            R<List<Long>> result = sysUserClient.listUserIdsByDeptIds(deptIds);
            if (result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("调用系统服务获取部门用户失败：deptIds={}", deptIds, e);
        }
        return Collections.emptyList();
    }
    
    /**
     * 根据角色 ID 列表获取用户 ID 列表
     */
    private List<Long> getUserIdsByRoleIds(List<Long> roleIds) {
        try {
            R<List<Long>> result = sysUserClient.listUserIdsByRoleIds(roleIds);
            if (result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("调用系统服务获取角色用户失败：roleIds={}", roleIds, e);
        }
        return Collections.emptyList();
    }
    
    /**
     * 根据职位 ID 列表获取用户 ID 列表
     */
    private List<Long> getUserIdsByPositionIds(List<Long> positionIds) {
        try {
            R<List<Long>> result = sysUserClient.listUserIdsByPositionIds(positionIds);
            if (result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("调用系统服务获取职位用户失败：positionIds={}", positionIds, e);
        }
        return Collections.emptyList();
    }
    
    @Override
    public Long evaluateBranchConditions(Long nodeId, String formContent) {
        // 获取节点配置
        WfTaskNodeConfig node = nodeConfigMapper.selectById(nodeId);
        if (node == null || node.getNodeType() != 5) { // 不是分支节点
            return null;
        }
        
        // 解析分支条件
        String conditionsJson = node.getBranchConditions();
        if (!StringUtils.hasText(conditionsJson)) {
            return null;
        }
        
        JSONObject conditionsObj = JSON.parseObject(conditionsJson);
        JSONArray conditions = conditionsObj.getJSONArray("conditions");
        
        if (conditions == null || conditions.isEmpty()) {
            // 返回默认节点
            return conditionsObj.getLong("defaultNodeId");
        }
        
        // 解析表单内容
        JSONObject formData = JSON.parseObject(formContent);
        
        // 评估条件
        for (int i = 0; i < conditions.size(); i++) {
            JSONObject condition = conditions.getJSONObject(i);
            String field = condition.getString("field");
            String operator = condition.getString("operator");
            String value = condition.getString("value");
            Long nextNodeId = condition.getLong("nextNodeId");
            
            if (evaluateCondition(formData, field, operator, value)) {
                return nextNodeId;
            }
        }
        
        // 返回默认节点
        return conditionsObj.getLong("defaultNodeId");
    }
    
    @Override
    public Boolean isNodeCompleted(Long executionId, Long nodeId) {
        // 获取该节点的所有审批人
        Long tenantId = getCurrentTenantId();
        Long[] approvers = determineApprovers(nodeId, tenantId);
        
        if (approvers.length == 0) {
            // 没有审批人，直接完成
            return true;
        }
        
        // 获取该节点的已审批人数（同意的）
        LambdaQueryWrapper<WfTaskExecutionApprover> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskExecutionApprover::getExecutionId, executionId)
               .eq(WfTaskExecutionApprover::getNodeId, nodeId);
        
        List<WfTaskExecutionApprover> approverList = executionApproverMapper.selectList(wrapper);
        
        // 获取节点审批类型
        WfTaskNodeConfig node = nodeConfigMapper.selectById(nodeId);
        if (node == null || node.getApproveType() == null) {
            return false;
        }
        
        // 统计同意的人数
        long approvedCount = approverList.stream()
                .filter(a -> a.getApproverDetail() != null)
                .filter(a -> {
                    JSONArray details = JSON.parseArray(a.getApproverDetail());
                    if (details == null || details.isEmpty()) {
                        return false;
                    }
                    // 检查是否有同意的记录
                    for (int i = 0; i < details.size(); i++) {
                        JSONObject detail = details.getJSONObject(i);
                        if (detail.getInteger("approveStatus") == 1) {
                            return true;
                        }
                    }
                    return false;
                })
                .count();
        
        switch (node.getApproveType()) {
            case 1: // 会签：所有人都要审批且同意
                return approvedCount >= approvers.length;
            case 2: // 或签：一人审批同意即可
                return approvedCount > 0;
            case 3: // 抄送：不需要审批
                return true;
            case 4: // 会签投票：超过半数同意
                return approvedCount > approvers.length / 2;
            case 5: // 逐个审批：按顺序，所有人都要审批
                return approvedCount >= approvers.length;
            default:
                return false;
        }
    }
    
    /**
     * 获取当前租户 ID
     */
    private Long getCurrentTenantId() {
        // TODO: 从租户上下文获取
        return 1L;
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
        
        // 获取任务配置
        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
        
        // 调用解释器
        callInterpreter(taskConfig.getInterpreterBean(), (interpreter, context) -> {
            context.setExecutionId(executionId);
            context.setStatus(status);
            interpreter.onEnd(context);
        });
        
        // 触发回调
        callbackService.triggerCallback(executionId, status);
        
        log.info("审批流程结束，executionId={}, status={}", executionId, status);
    }
    
    /**
     * 查找下一节点
     */
    private Long findNextNode(Long executionId, WfTaskNodeConfig currentNode, WfTaskConfig taskConfig) {
        // 如果是分支节点，需要评估条件
        if (currentNode.getNodeType() == 5) { // 分支节点
            WfTaskExecution execution = executionMapper.selectById(executionId);
            return evaluateBranchConditions(currentNode.getId(), execution.getFormContent());
        }
        
        // 获取后置节点
        String nextNodeIds = currentNode.getNextNodeIds();
        if (!StringUtils.hasText(nextNodeIds)) {
            return null;
        }
        
        JSONArray array = JSON.parseArray(nextNodeIds);
        if (array.isEmpty()) {
            return null;
        }
        
        // 返回第一个后置节点
        return array.getLong(0);
    }
    
    /**
     * 评估单个条件
     */
    private Boolean evaluateCondition(JSONObject formData, String field, String operator, String value) {
        Object fieldValue = formData.get(field);
        if (fieldValue == null) {
            return false;
        }
        
        switch (operator) {
            case "=":
                return String.valueOf(fieldValue).equals(value);
            case "!=":
                return !String.valueOf(fieldValue).equals(value);
            case ">":
                return Double.valueOf(String.valueOf(fieldValue)) > Double.valueOf(value);
            case ">=":
                return Double.valueOf(String.valueOf(fieldValue)) >= Double.valueOf(value);
            case "<":
                return Double.valueOf(String.valueOf(fieldValue)) < Double.valueOf(value);
            case "<=":
                return Double.valueOf(String.valueOf(fieldValue)) <= Double.valueOf(value);
            case "like":
                return String.valueOf(fieldValue).contains(value);
            case "in":
                JSONArray inArray = JSON.parseArray(value);
                return inArray.contains(fieldValue);
            default:
                return false;
        }
    }
    
    /**
     * 调用审批解释器
     */
    private void callInterpreter(String beanName, InterpreterAction action) {
        IApprovalInterpreter interpreter = interpreterRegistry.getInterpreter(beanName);
        if (interpreter == null) {
            // 使用空实现
            interpreter = interpreterRegistry.getInterpreter("emptyApprovalInterpreter");
        }
        
        if (interpreter != null) {
            ApprovalContext context = new ApprovalContext();
            action.execute(interpreter, context);
        }
    }
    
    /**
     * 解释器执行函数式接口
     */
    @FunctionalInterface
    private interface InterpreterAction {
        void execute(IApprovalInterpreter interpreter, ApprovalContext context);
    }
}
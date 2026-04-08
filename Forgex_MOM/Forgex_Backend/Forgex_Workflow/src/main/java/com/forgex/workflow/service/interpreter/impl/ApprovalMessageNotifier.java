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
package com.forgex.workflow.service.interpreter.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.forgex.common.message.MessageSenderService;
import com.forgex.workflow.domain.entity.WfTaskConfig;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.domain.entity.WfTaskExecutionApprover;
import com.forgex.workflow.domain.entity.WfTaskNodeApprover;
import com.forgex.workflow.mapper.WfTaskExecutionApproverMapper;
import com.forgex.workflow.mapper.WfTaskExecutionMapper;
import com.forgex.workflow.mapper.WfTaskNodeApproverMapper;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.service.interpreter.ApprovalContext;
import com.forgex.workflow.service.interpreter.IApprovalInterpreter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 审批消息通知解释器。
 * <p>
 * 在审批流程的关键节点（开始、通过、驳回、完成）自动发送消息通知。
 * 支持配置不同的消息模板，并通过链接跳转到审批服务页面。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Slf4j
@Component("approvalMessageNotifier")
@RequiredArgsConstructor
public class ApprovalMessageNotifier implements IApprovalInterpreter {

    private final MessageSenderService messageSenderService;
    private final WfTaskExecutionMapper executionMapper;
    private final WfTaskConfigMapper taskConfigMapper;
    private final WfTaskNodeApproverMapper nodeApproverMapper;
    private final WfTaskExecutionApproverMapper executionApproverMapper;

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onStart(ApprovalContext context) {
        log.info("审批开始消息通知：executionId={}, taskName={}", 
                context.getExecutionId(), context.getTaskName());
        
        try {
            // 获取任务配置
            WfTaskConfig taskConfig = taskConfigMapper.selectById(context.getTaskConfigId());
            if (taskConfig == null) {
                log.warn("任务配置不存在，无法发送开始通知：taskConfigId={}", context.getTaskConfigId());
                return;
            }
            
            // 检查是否配置了审批开始消息模板
            if (!hasText(taskConfig.getStartMessageTemplateCode())) {
                log.info("未配置审批开始消息模板，跳过发送：taskCode={}", taskConfig.getTaskCode());
                return;
            }
            
            // 构建消息数据
            Map<String, Object> dataMap = buildStartMessageData(context, taskConfig);
            
            // 发送消息
            messageSenderService.sendMessageAsync(
                taskConfig.getStartMessageTemplateCode(), 
                dataMap, 
                "系统", 
                "WORKFLOW_APPROVAL"
            );
            
            log.info("审批开始消息发送成功：executionId={}, templateCode={}", 
                    context.getExecutionId(), taskConfig.getStartMessageTemplateCode());
        } catch (Exception e) {
            log.error("发送审批开始消息失败：executionId={}, error={}", 
                    context.getExecutionId(), e.getMessage(), e);
        }
    }

    @Override
    public void onApprove(ApprovalContext context) {
        log.info("审批通过消息通知：executionId={}, approverName={}", 
                context.getExecutionId(), context.getApproverName());
        
        try {
            // 获取执行记录
            WfTaskExecution execution = executionMapper.selectById(context.getExecutionId());
            if (execution == null) {
                log.warn("执行记录不存在，无法发送通过通知：executionId={}", context.getExecutionId());
                return;
            }
            
            // 获取任务配置
            WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
            if (taskConfig == null) {
                log.warn("任务配置不存在，无法发送通过通知：taskConfigId={}", execution.getTaskConfigId());
                return;
            }
            
            // 检查是否配置了审批通过消息模板
            if (!hasText(taskConfig.getApproveMessageTemplateCode())) {
                log.info("未配置审批通过消息模板，跳过发送：taskCode={}", taskConfig.getTaskCode());
                return;
            }
            
            // 构建消息数据
            Map<String, Object> dataMap = buildApproveMessageData(context, execution);
            
            // 发送消息
            messageSenderService.sendMessageAsync(
                taskConfig.getApproveMessageTemplateCode(), 
                dataMap, 
                "系统", 
                "WORKFLOW_APPROVAL"
            );
            
            log.info("审批通过消息发送成功：executionId={}, templateCode={}", 
                    context.getExecutionId(), taskConfig.getApproveMessageTemplateCode());
        } catch (Exception e) {
            log.error("发送审批通过消息失败：executionId={}, error={}", 
                    context.getExecutionId(), e.getMessage(), e);
        }
    }

    @Override
    public void onReject(ApprovalContext context) {
        log.info("审批驳回消息通知：executionId={}, approverName={}", 
                context.getExecutionId(), context.getApproverName());
        
        try {
            // 获取执行记录
            WfTaskExecution execution = executionMapper.selectById(context.getExecutionId());
            if (execution == null) {
                log.warn("执行记录不存在，无法发送驳回通知：executionId={}", context.getExecutionId());
                return;
            }
            
            // 获取任务配置
            WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
            if (taskConfig == null) {
                log.warn("任务配置不存在，无法发送驳回通知：taskConfigId={}", execution.getTaskConfigId());
                return;
            }
            
            // 检查是否配置了审批驳回消息模板
            if (!hasText(taskConfig.getRejectMessageTemplateCode())) {
                log.info("未配置审批驳回消息模板，跳过发送：taskCode={}", taskConfig.getTaskCode());
                return;
            }
            
            // 构建消息数据
            Map<String, Object> dataMap = buildRejectMessageData(context, execution);
            
            // 发送消息
            messageSenderService.sendMessageAsync(
                taskConfig.getRejectMessageTemplateCode(), 
                dataMap, 
                "系统", 
                "WORKFLOW_APPROVAL"
            );
            
            log.info("审批驳回消息发送成功：executionId={}, templateCode={}", 
                    context.getExecutionId(), taskConfig.getRejectMessageTemplateCode());
        } catch (Exception e) {
            log.error("发送审批驳回消息失败：executionId={}, error={}", 
                    context.getExecutionId(), e.getMessage(), e);
        }
    }

    @Override
    public void onEnd(ApprovalContext context) {
        log.info("审批完成消息通知：executionId={}, status={}", 
                context.getExecutionId(), context.getStatus());
        
        try {
            // 获取执行记录
            WfTaskExecution execution = executionMapper.selectById(context.getExecutionId());
            if (execution == null) {
                log.warn("执行记录不存在，无法发送完成通知：executionId={}", context.getExecutionId());
                return;
            }
            
            // 获取任务配置
            WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
            if (taskConfig == null) {
                log.warn("任务配置不存在，无法发送完成通知：taskConfigId={}", execution.getTaskConfigId());
                return;
            }
            
            // 检查是否配置了审批完成消息模板
            if (!hasText(taskConfig.getFinishMessageTemplateCode())) {
                log.info("未配置审批完成消息模板，跳过发送：taskCode={}", taskConfig.getTaskCode());
                return;
            }
            
            // 构建消息数据
            Map<String, Object> dataMap = buildFinishMessageData(context, execution);
            
            // 发送消息
            messageSenderService.sendMessageAsync(
                taskConfig.getFinishMessageTemplateCode(), 
                dataMap, 
                "系统", 
                "WORKFLOW_APPROVAL"
            );
            
            log.info("审批完成消息发送成功：executionId={}, templateCode={}", 
                    context.getExecutionId(), taskConfig.getFinishMessageTemplateCode());
        } catch (Exception e) {
            log.error("发送审批完成消息失败：executionId={}, error={}", 
                    context.getExecutionId(), e.getMessage(), e);
        }
    }

    /**
     * 构建审批开始消息数据
     * 
     * @param context 审批上下文
     * @param taskConfig 任务配置
     * @return 消息数据 Map
     */
    private Map<String, Object> buildStartMessageData(ApprovalContext context, WfTaskConfig taskConfig) {
        Map<String, Object> dataMap = new HashMap<>();
        
        // 基本信息
        dataMap.put("executionId", context.getExecutionId());
        dataMap.put("taskName", context.getTaskName());
        dataMap.put("taskCode", context.getTaskCode());
        dataMap.put("initiatorName", context.getInitiatorName());
        dataMap.put("initiatorId", context.getInitiatorId());
        dataMap.put("startTime", LocalDateTime.now().format(DATETIME_FORMATTER));
        
        // 表单内容
        dataMap.put("formContent", context.getFormContent());
        
        // 构建跳转链接
        String linkUrl = buildApprovalLink(taskConfig.getLinkBaseUrl(), context.getExecutionId());
        dataMap.put("linkUrl", linkUrl);
        
        // 获取当前节点的审批人信息
        if (context.getCurrentNodeId() != null) {
            Long[] approvers = determineApprovers(context.getCurrentNodeId(), taskConfig.getTenantId());
            if (approvers != null && approvers.length > 0) {
                dataMap.put("currentApprovers", Arrays.toString(approvers));
            }
        }
        
        return dataMap;
    }

    /**
     * 构建审批通过消息数据
     * 
     * @param context 审批上下文
     * @param execution 执行记录
     * @return 消息数据 Map
     */
    private Map<String, Object> buildApproveMessageData(ApprovalContext context, WfTaskExecution execution) {
        Map<String, Object> dataMap = new HashMap<>();
        
        // 基本信息
        dataMap.put("executionId", context.getExecutionId());
        dataMap.put("taskName", context.getTaskName());
        dataMap.put("taskCode", context.getTaskCode());
        dataMap.put("initiatorName", execution.getInitiatorName());
        dataMap.put("initiatorId", execution.getInitiatorId());
        
        // 审批人信息
        dataMap.put("approverName", context.getApproverName());
        dataMap.put("approverId", context.getApproverId());
        dataMap.put("comment", context.getComment());
        dataMap.put("approveTime", context.getApproveTime() != null ? 
                context.getApproveTime().format(DATETIME_FORMATTER) : 
                LocalDateTime.now().format(DATETIME_FORMATTER));
        
        // 表单内容
        dataMap.put("formContent", execution.getFormContent());
        
        // 构建跳转链接
        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
        String linkUrl = buildApprovalLink(taskConfig != null ? taskConfig.getLinkBaseUrl() : null, 
                                           context.getExecutionId());
        dataMap.put("linkUrl", linkUrl);
        
        return dataMap;
    }

    /**
     * 构建审批驳回消息数据
     * 
     * @param context 审批上下文
     * @param execution 执行记录
     * @return 消息数据 Map
     */
    private Map<String, Object> buildRejectMessageData(ApprovalContext context, WfTaskExecution execution) {
        Map<String, Object> dataMap = new HashMap<>();
        
        // 基本信息
        dataMap.put("executionId", context.getExecutionId());
        dataMap.put("taskName", context.getTaskName());
        dataMap.put("taskCode", context.getTaskCode());
        dataMap.put("initiatorName", execution.getInitiatorName());
        dataMap.put("initiatorId", execution.getInitiatorId());
        
        // 审批人信息
        dataMap.put("approverName", context.getApproverName());
        dataMap.put("approverId", context.getApproverId());
        dataMap.put("rejectReason", context.getComment());
        dataMap.put("rejectTime", LocalDateTime.now().format(DATETIME_FORMATTER));
        
        // 表单内容
        dataMap.put("formContent", execution.getFormContent());
        
        // 构建跳转链接
        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
        String linkUrl = buildApprovalLink(taskConfig != null ? taskConfig.getLinkBaseUrl() : null, 
                                           context.getExecutionId());
        dataMap.put("linkUrl", linkUrl);
        
        return dataMap;
    }

    /**
     * 构建审批完成消息数据
     * 
     * @param context 审批上下文
     * @param execution 执行记录
     * @return 消息数据 Map
     */
    private Map<String, Object> buildFinishMessageData(ApprovalContext context, WfTaskExecution execution) {
        Map<String, Object> dataMap = new HashMap<>();
        
        // 基本信息
        dataMap.put("executionId", context.getExecutionId());
        dataMap.put("taskName", context.getTaskName());
        dataMap.put("taskCode", context.getTaskCode());
        dataMap.put("initiatorName", execution.getInitiatorName());
        dataMap.put("initiatorId", execution.getInitiatorId());
        
        // 最终状态
        String finishStatus = context.getStatus() == 2 ? "审批通过" : "审批驳回";
        dataMap.put("finishStatus", finishStatus);
        dataMap.put("finishTime", execution.getEndTime() != null ? 
                execution.getEndTime().format(DATETIME_FORMATTER) : 
                LocalDateTime.now().format(DATETIME_FORMATTER));
        
        // 表单内容
        dataMap.put("formContent", execution.getFormContent());
        
        // 构建跳转链接
        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
        String linkUrl = buildApprovalLink(taskConfig != null ? taskConfig.getLinkBaseUrl() : null, 
                                           context.getExecutionId());
        dataMap.put("linkUrl", linkUrl);
        
        return dataMap;
    }

    /**
     * 构建审批详情链接
     * 
     * @param linkBaseUrl 基础 URL
     * @param executionId 执行 ID
     * @return 完整链接
     */
    private String buildApprovalLink(String linkBaseUrl, Long executionId) {
        if (!hasText(linkBaseUrl)) {
            // 默认使用前端审批详情页面
            linkBaseUrl = "http://localhost:5173/workflow/approval/detail/";
        }
        
        // 确保 URL 以 / 结尾
        if (!linkBaseUrl.endsWith("/")) {
            linkBaseUrl += "/";
        }
        
        return linkBaseUrl + executionId;
    }

    /**
     * 确定节点审批人
     * 
     * @param nodeId 节点 ID
     * @param tenantId 租户 ID
     * @return 审批人 ID 数组
     */
    private Long[] determineApprovers(Long nodeId, Long tenantId) {
        try {
            // 获取节点审批人配置
            List<WfTaskNodeApprover> approvers = nodeApproverMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WfTaskNodeApprover>()
                    .eq(WfTaskNodeApprover::getNodeConfigId, nodeId)
                    .eq(WfTaskNodeApprover::getTenantId, tenantId)
                    .eq(WfTaskNodeApprover::getDeleted, false)
            );
            
            if (approvers.isEmpty()) {
                return new Long[0];
            }
            
            // 合并所有审批人（去重）
            Set<Long> approverIds = new HashSet<>();
            for (WfTaskNodeApprover approver : approvers) {
                if (hasText(approver.getApproverIds())) {
                    JSONArray array = JSON.parseArray(approver.getApproverIds());
                    for (int i = 0; i < array.size(); i++) {
                        approverIds.add(array.getLong(i));
                    }
                }
            }
            
            return approverIds.toArray(new Long[0]);
        } catch (Exception e) {
            log.error("确定审批人失败：nodeId={}, error={}", nodeId, e.getMessage());
            return new Long[0];
        }
    }

    /**
     * 判断字符串是否有内容
     * 
     * @param str 字符串
     * @return 是否有内容
     */
    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}

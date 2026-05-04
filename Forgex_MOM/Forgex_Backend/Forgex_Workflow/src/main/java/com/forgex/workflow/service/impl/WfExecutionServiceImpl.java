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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.api.service.UserInfoService;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.workflow.common.WorkflowConstants;
import com.forgex.workflow.domain.dto.WfDashboardAnalyticsVO;
import com.forgex.workflow.domain.dto.WfDashboardSummaryVO;
import com.forgex.workflow.domain.dto.WfDashboardUserShareDTO;
import com.forgex.workflow.domain.dto.WfDashboardWeeklyResultDTO;
import com.forgex.workflow.domain.dto.WfApprovalActionLogDTO;
import com.forgex.workflow.domain.dto.WfApprovalInstanceDTO;
import com.forgex.workflow.domain.dto.WfExecutionDTO;
import com.forgex.workflow.domain.entity.WfMyTask;
import com.forgex.workflow.domain.entity.WfTaskApprovalActionLog;
import com.forgex.workflow.domain.entity.WfTaskApprovalInstance;
import com.forgex.workflow.domain.entity.WfTaskNodeConfig;
import com.forgex.workflow.domain.entity.WfTaskConfig;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.domain.entity.WfTaskExecutionApprover;
import com.forgex.workflow.domain.entity.WfTaskExecutionDetail;
import com.forgex.workflow.domain.param.WfExecutionAddSignParam;
import com.forgex.workflow.domain.param.WfExecutionApproveParam;
import com.forgex.workflow.domain.param.WfExecutionBatchApproveParam;
import com.forgex.workflow.domain.param.WfExecutionBatchRemindParam;
import com.forgex.workflow.domain.param.WfExecutionBatchTransferParam;
import com.forgex.workflow.domain.param.WfExecutionCompensateParam;
import com.forgex.workflow.domain.param.WfExecutionDelegateSaveParam;
import com.forgex.workflow.domain.param.WfExecutionQueryParam;
import com.forgex.workflow.domain.param.WfExecutionStartParam;
import com.forgex.workflow.domain.param.WfExecutionTransferParam;
import com.forgex.workflow.enums.WorkflowPromptEnum;
import com.forgex.workflow.mapper.WfMyTaskMapper;
import com.forgex.workflow.mapper.WfTaskApprovalActionLogMapper;
import com.forgex.workflow.mapper.WfTaskApprovalInstanceMapper;
import com.forgex.workflow.mapper.WfTaskNodeConfigMapper;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.mapper.WfTaskExecutionApproverMapper;
import com.forgex.workflow.mapper.WfTaskExecutionDetailMapper;
import com.forgex.workflow.mapper.WfTaskExecutionMapper;
import com.forgex.workflow.service.IWfEngineService;
import com.forgex.workflow.service.IWfExecutionService;
import com.forgex.workflow.service.WorkflowNotificationService;
import com.forgex.workflow.service.handler.ApprovalActionHandlerFactory;
import com.forgex.workflow.service.handler.ApprovalActionHandlerType;
import com.forgex.workflow.service.interpreter.ApprovalContext;
import com.forgex.workflow.service.interpreter.ApprovalInterpreterRegistry;
import com.forgex.workflow.service.interpreter.IApprovalInterpreter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审批执行服务实现类。
 * <p>
 * 实现 {@link IWfExecutionService} 接口，提供审批执行实例的完整业务逻辑，
 * 包括发起审批、审批处理（同意/驳回/转交/加签）、批量操作、查询列表等功能。
 * </p>
 * <p>
 * 主要职责：
 * </p>
 * <ul>
 *   <li>审批实例的发起和启动</li>
 *   <li>审批操作处理（同意、驳回、转交、加签、撤销）</li>
 *   <li>批量审批、批量转交、批量提醒</li>
 *   <li>我的发起、我的待办、我的已办、我的抄送查询</li>
 *   <li>审批工作台数据加载</li>
 * </ul>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-04-01
 * @see IWfExecutionService
 * @see WfTaskExecution
 * @see WfTaskApprovalInstance
 */
@Slf4j
@Service
@DS("workflow")
@RequiredArgsConstructor
public class WfExecutionServiceImpl implements IWfExecutionService {

    /**
     * 业务日「昨日」等计算使用的时区（与前端展示习惯一致）。
     */
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 审批工作台首页每类展示条数上限。
     */
    private static final int DASHBOARD_LIST_LIMIT = 6;

    private static final DateTimeFormatter APPROVE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DASHBOARD_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final WfTaskExecutionMapper executionMapper;
    private final WfTaskExecutionDetailMapper executionDetailMapper;
    private final WfTaskExecutionApproverMapper executionApproverMapper;
    private final WfMyTaskMapper myTaskMapper;
    private final WfTaskConfigMapper taskConfigMapper;
    private final WfTaskNodeConfigMapper nodeConfigMapper;
    private final WfTaskApprovalInstanceMapper approvalInstanceMapper;
    private final WfTaskApprovalActionLogMapper approvalActionLogMapper;
    private final IWfEngineService engineService;
    private final UserInfoService userInfoService;
    private final ApprovalInterpreterRegistry interpreterRegistry;
    private final WorkflowNotificationService workflowNotificationService;
    /**
     * 延迟获取动作处理器工厂，避免“执行服务 -> 工厂 -> 处理器 -> 执行服务”循环依赖。
     */
    private final ObjectProvider<ApprovalActionHandlerFactory> approvalActionHandlerFactoryProvider;

    /**
     * 发起审批流程。
     * <p>
     * 根据任务编码查找已发布的任务配置，创建审批执行记录并初始化流程实例。
     * </p>
     * <p>
     * 主要步骤：
     * </p>
     * <ol>
     *   <li>校验当前用户和租户信息</li>
     *   <li>查询已发布的任务配置</li>
     *   <li>校验表单配置和表单内容</li>
     *   <li>创建执行记录</li>
     *   <li>调用引擎服务初始化流程</li>
     * </ol>
     *
     * @param param 发起审批参数，包含任务编码、表单内容、选择的审批人等
     * @return 执行记录 ID
     * @throws I18nBusinessException 当任务配置不存在、已禁用或参数不合法时抛出业务异常
     * @see WfExecutionStartParam
     * @see WfTaskExecution
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startExecution(WfExecutionStartParam param) {
        Long currentUserId = requireCurrentUserId();
        Long currentTenantId = requireCurrentTenantId();

        // 查询已发布的任务配置
        WfTaskConfig taskConfig = taskConfigMapper.selectOne(new LambdaQueryWrapper<WfTaskConfig>()
                .eq(WfTaskConfig::getTaskCode, param.getTaskCode())
                .eq(WfTaskConfig::getTenantId, currentTenantId)
                .eq(WfTaskConfig::getDeleted, false)
                .eq(WfTaskConfig::getConfigStage, WorkflowConstants.ConfigStage.PUBLISHED)
                .orderByDesc(WfTaskConfig::getVersion)
                .last("LIMIT 1"));
        if (taskConfig == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_TASK_CONFIG_NOT_FOUND);
        }
        if (Objects.equals(taskConfig.getStatus(), 0)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_TASK_DISABLED);
        }

        // 校验表单配置
        if (Objects.equals(taskConfig.getFormType(), 2) && !StringUtils.hasText(taskConfig.getFormContent())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_LOW_CODE_FORM_NOT_CONFIGURED);
        }
        // 校验表单内容
        if (!StringUtils.hasText(param.getFormContent())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_FORM_CONTENT_REQUIRED);
        }
        try {
            JSON.parseObject(param.getFormContent());
        } catch (Exception ex) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_FORM_CONTENT_INVALID_JSON);
        }

        // 创建执行记录
        WfTaskExecution execution = new WfTaskExecution();
        execution.setTaskConfigId(taskConfig.getId());
        execution.setTaskCode(taskConfig.getTaskCode());
        execution.setTaskName(taskConfig.getTaskName());
        execution.setFormContent(param.getFormContent());
        execution.setStatus(1);
        execution.setStartTime(LocalDateTime.now());
        execution.setInitiatorId(currentUserId);
        execution.setInitiatorName(resolveUsername(currentUserId));
        execution.setTenantId(currentTenantId);
        executionMapper.insert(execution);

        // 初始化流程
        engineService.initExecution(execution.getId(), taskConfig.getId(), param.getSelectedApprovers());

        log.info("发起审批成功，executionId={}, taskCode={}, initiatorId={}",
                execution.getId(), taskConfig.getTaskCode(), currentUserId);
        return execution.getId();
    }

    /**
     * 审批同意。
     * <p>
     * 处理审批人的同意操作，记录审批意见并流转至下一个节点。
     * </p>
     * <p>
     * 主要步骤：
     * </p>
     * <ol>
     *   <li>校验执行记录和当前节点</li>
     *   <li>保存审批记录</li>
     *   <li>更新审批实例状态</li>
     *   <li>触发审批通过解释器</li>
     *   <li>判断节点是否完成并流转</li>
     *   <li>发送审批通过通知</li>
     * </ol>
     *
     * @param param 审批参数，包含执行 ID、审批实例 ID、审批意见等
     * @return 审批结果，成功返回 true
     * @throws I18nBusinessException 当执行记录不存在、用户无权限或参数不合法时抛出业务异常
     * @see WfExecutionApproveParam
     * @see WfTaskApprovalInstance
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(WfExecutionApproveParam param) {
        Long currentUserId = requireCurrentUserId();
        WfTaskExecution execution = requireRunningExecution(param.getExecutionId());
        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());

        Long currentNodeId = execution.getCurrentNodeId();
        String currentNodeName = execution.getCurrentNodeName();
        if (currentNodeId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_CURRENT_NODE_NOT_FOUND);
        }

        // 校验待办任务和审批实例
        WfMyTask pendingTask = requirePendingTask(execution.getId(), currentNodeId, currentUserId);
        WfTaskExecutionDetail currentDetail = requireCurrentExecutionDetail(execution.getId(), currentNodeId);
        String approverName = resolveUsername(currentUserId);
        LocalDateTime approveTime = LocalDateTime.now();
        WfTaskApprovalInstance approvalInstance = resolveApprovalInstance(pendingTask, param.getApprovalInstanceId(), currentUserId);

        // 保存审批记录
        saveApprovalRecord(currentDetail, execution, currentUserId, approverName, 1, param.getComment(), null, approveTime);
        // 更新审批实例状态
        updateAfterInstanceAction(execution, currentDetail, pendingTask, approvalInstance,
                WorkflowConstants.ApprovalInstanceStatus.APPROVED,
                WorkflowConstants.ApprovalActionType.APPROVE,
                param.getComment(), approverName, approveTime);

        // 触发审批通过解释器
        callInterpreter(taskConfig == null ? null : taskConfig.getInterpreterBean(), (interpreter, context) -> {
            populateContext(context, execution, taskConfig, currentNodeId, currentNodeName);
            context.setApproverId(currentUserId);
            context.setApproverName(approverName);
            context.setApproveStatus(1);
            context.setComment(param.getComment());
            context.setApproveTime(approveTime);
            interpreter.onApprove(context);
        });

        // 或签只需要任意一人同意，通过后立即关闭同节点其他未处理实例和待办。
        closeOtherPendingInstancesIfOrSign(
                currentDetail,
                approvalInstance,
                WorkflowConstants.ApprovalInstanceStatus.APPROVED);

        // 判断节点是否完成并流转
        boolean nodeCompleted = Boolean.TRUE.equals(engineService.isNodeCompleted(execution.getId(), currentNodeId));
        if (nodeCompleted) {
            engineService.moveToNextNode(execution.getId(), currentNodeId, 1, null);
        }

        // 发送审批通过通知
        if (nodeCompleted) {
            WfTaskExecution latestExecution = executionMapper.selectById(execution.getId());
            if (latestExecution != null && !Objects.equals(latestExecution.getStatus(), 2)) {
                workflowNotificationService.notifyApproved(
                        latestExecution,
                        currentNodeName,
                        approverName,
                        param.getComment()
                );
            }
        }

        log.info("审批同意成功，executionId={}, nodeId={}, approverId={}",
                execution.getId(), currentNodeId, currentUserId);
        return true;
    }

    /**
     * 审批驳回。
     * <p>
     * 处理审批人的驳回操作，记录驳回意见并退回或结束流程。
     * </p>
     * <p>
     * 主要步骤：
     * </p>
     * <ol>
     *   <li>校验执行记录和当前节点</li>
     *   <li>保存审批记录</li>
     *   <li>更新审批实例状态</li>
     *   <li>触发审批驳回解释器</li>
     *   <li>根据驳回类型流转（退回上一步/驳回结束）</li>
     *   <li>发送审批驳回通知</li>
     * </ol>
     *
     * @param param 审批参数，包含执行 ID、审批实例 ID、驳回类型、驳回意见等
     * @return 审批结果，成功返回 true
     * @throws I18nBusinessException 当执行记录不存在、用户无权限或参数不合法时抛出业务异常
     * @see WfExecutionApproveParam
     * @see WfTaskApprovalInstance
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reject(WfExecutionApproveParam param) {
        Long currentUserId = requireCurrentUserId();
        WfTaskExecution execution = requireRunningExecution(param.getExecutionId());
        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());

        Long currentNodeId = execution.getCurrentNodeId();
        String currentNodeName = execution.getCurrentNodeName();
        if (currentNodeId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_CURRENT_NODE_NOT_FOUND);
        }

        Integer rejectType = param.getRejectType() == null ? 1 : param.getRejectType();
        WfMyTask pendingTask = requirePendingTask(execution.getId(), currentNodeId, currentUserId);
        WfTaskExecutionDetail currentDetail = requireCurrentExecutionDetail(execution.getId(), currentNodeId);
        String approverName = resolveUsername(currentUserId);
        LocalDateTime approveTime = LocalDateTime.now();
        WfTaskApprovalInstance approvalInstance = resolveApprovalInstance(pendingTask, param.getApprovalInstanceId(), currentUserId);

        // 保存审批记录
        saveApprovalRecord(currentDetail, execution, currentUserId, approverName, 2, param.getComment(), rejectType, approveTime);
        // 更新审批实例状态
        updateAfterInstanceAction(execution, currentDetail, pendingTask, approvalInstance,
                WorkflowConstants.ApprovalInstanceStatus.REJECTED,
                WorkflowConstants.ApprovalActionType.REJECT,
                param.getComment(), approverName, approveTime);

        // 触发审批驳回解释器
        callInterpreter(taskConfig == null ? null : taskConfig.getInterpreterBean(), (interpreter, context) -> {
            populateContext(context, execution, taskConfig, currentNodeId, currentNodeName);
            context.setApproverId(currentUserId);
            context.setApproverName(approverName);
            context.setApproveStatus(2);
            context.setComment(param.getComment());
            context.setApproveTime(approveTime);
            interpreter.onReject(context);
        });

        // 根据驳回类型流转
        engineService.moveToNextNode(execution.getId(), currentNodeId, 2, rejectType);
        // 发送审批驳回通知
        WfTaskExecution latestExecution = executionMapper.selectById(execution.getId());
        workflowNotificationService.notifyRejected(
                latestExecution == null ? execution : latestExecution,
                currentNodeName,
                approverName,
                param.getComment(),
                rejectType
        );

        log.info("审批驳回成功，executionId={}, nodeId={}, approverId={}, rejectType={}",
                execution.getId(), currentNodeId, currentUserId, rejectType);
        return true;
    }

    /**
     * 转交审批任务。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean transfer(WfExecutionTransferParam param) {
        return getApprovalActionHandlerFactory()
                .<WfExecutionTransferParam>getHandler(ApprovalActionHandlerType.TRANSFER)
                .handle(param);
    }

    /**
     * 处理transferaction。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    public Boolean handleTransferAction(WfExecutionTransferParam param) {
        Long currentUserId = requireCurrentUserId();
        WfTaskExecution execution = requireRunningExecution(param.getExecutionId());
        WfTaskApprovalInstance currentInstance = requirePendingApprovalInstance(param.getApprovalInstanceId(), execution.getId());
        if (!Objects.equals(currentInstance.getApproverId(), currentUserId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_USER_NO_PERMISSION_TRANSFER);
        }
        if (Objects.equals(currentInstance.getApproverId(), param.getTargetApproverId())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_TRANSFER_TARGET_SAME_AS_CURRENT);
        }

        closePendingTaskByInstanceId(currentInstance.getId());

        currentInstance.setStatus(WorkflowConstants.ApprovalInstanceStatus.TRANSFERRED);
        currentInstance.setActionType(WorkflowConstants.ApprovalActionType.TRANSFER);
        currentInstance.setComment(param.getComment());
        currentInstance.setApproveTime(LocalDateTime.now());
        approvalInstanceMapper.updateById(currentInstance);

        WfTaskApprovalInstance targetInstance = buildPendingInstanceFromCurrent(currentInstance, param.getTargetApproverId());
        targetInstance.setTransferFromUserId(currentUserId);
        approvalInstanceMapper.insert(targetInstance);

        createPendingTaskFromInstance(execution, requireCurrentExecutionDetail(execution.getId(), currentInstance.getNodeId()), targetInstance);
        insertActionLog(execution, currentInstance.getExecutionDetailId(), currentInstance.getNodeId(), currentInstance.getId(),
                WorkflowConstants.ApprovalActionType.TRANSFER, currentUserId, resolveUsername(currentUserId),
                param.getTargetApproverId(), resolveUsername(param.getTargetApproverId()), param.getComment(), JSON.toJSONString(currentInstance));
        return true;
    }

    /**
     * 加签审批任务。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addSign(WfExecutionAddSignParam param) {
        return getApprovalActionHandlerFactory()
                .<WfExecutionAddSignParam>getHandler(ApprovalActionHandlerType.ADD_SIGN)
                .handle(param);
    }

    /**
     * 处理addsignaction。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    public Boolean handleAddSignAction(WfExecutionAddSignParam param) {
        Long currentUserId = requireCurrentUserId();
        WfTaskExecution execution = requireRunningExecution(param.getExecutionId());
        WfTaskApprovalInstance currentInstance = requirePendingApprovalInstance(param.getApprovalInstanceId(), execution.getId());
        if (!Objects.equals(currentInstance.getApproverId(), currentUserId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_USER_NO_PERMISSION_ADD_SIGN);
        }

        WfTaskApprovalInstance addSignInstance = buildPendingInstanceFromCurrent(currentInstance, param.getTargetApproverId());
        approvalInstanceMapper.insert(addSignInstance);

        createPendingTaskFromInstance(execution, requireCurrentExecutionDetail(execution.getId(), currentInstance.getNodeId()), addSignInstance);
        insertActionLog(execution, currentInstance.getExecutionDetailId(), currentInstance.getNodeId(), currentInstance.getId(),
                WorkflowConstants.ApprovalActionType.ADD_SIGN, currentUserId, resolveUsername(currentUserId),
                param.getTargetApproverId(), resolveUsername(param.getTargetApproverId()), param.getComment(), JSON.toJSONString(addSignInstance));
        return true;
    }

    /**
     * 批量审批任务。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchApprove(WfExecutionBatchApproveParam param) {
        if (param.getExecutionIds() == null) {
            return true;
        }
        for (Long executionId : param.getExecutionIds()) {
            validateExecutionExists(executionId);
            WfExecutionApproveParam approveParam = new WfExecutionApproveParam();
            approveParam.setExecutionId(executionId);
            approveParam.setApproveStatus(param.getApproveStatus());
            approveParam.setComment(param.getComment());
            if (Objects.equals(param.getApproveStatus(), 2)) {
                reject(approveParam);
            } else {
                approve(approveParam);
            }
        }
        return true;
    }

    /**
     * 批量转交审批任务。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchTransfer(WfExecutionBatchTransferParam param) {
        if (param.getExecutionIds() == null) {
            return true;
        }
        Long currentUserId = requireCurrentUserId();
        for (Long executionId : param.getExecutionIds()) {
            validateExecutionExists(executionId);
            WfTaskExecution execution = requireRunningExecution(executionId);
            WfMyTask myTask = requirePendingTask(executionId, execution.getCurrentNodeId(), currentUserId);
            if (myTask.getApprovalInstanceId() == null) {
                continue;
            }
            WfExecutionTransferParam transferParam = new WfExecutionTransferParam();
            transferParam.setExecutionId(executionId);
            transferParam.setApprovalInstanceId(myTask.getApprovalInstanceId());
            transferParam.setTargetApproverId(param.getTargetApproverId());
            transferParam.setComment(param.getComment());
            transfer(transferParam);
        }
        return true;
    }

    /**
     * 批量催办审批任务。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    public Boolean batchRemind(WfExecutionBatchRemindParam param) {
        Long currentUserId = requireCurrentUserId();
        if (param.getExecutionIds() != null) {
            for (Long executionId : param.getExecutionIds()) {
                WfTaskExecution execution = requireRunningExecution(executionId);
                List<WfTaskApprovalInstance> pendingInstances = approvalInstanceMapper.selectList(new LambdaQueryWrapper<WfTaskApprovalInstance>()
                        .eq(WfTaskApprovalInstance::getExecutionId, executionId)
                        .eq(WfTaskApprovalInstance::getDeleted, 0)
                        .eq(WfTaskApprovalInstance::getStatus, WorkflowConstants.ApprovalInstanceStatus.PENDING)
                        .eq(WfTaskApprovalInstance::getActivated, true));
                for (WfTaskApprovalInstance instance : pendingInstances) {
                    insertActionLog(execution, instance.getExecutionDetailId(), instance.getNodeId(), instance.getId(),
                            WorkflowConstants.ApprovalActionType.SYSTEM_CLOSE, currentUserId, resolveUsername(currentUserId),
                            instance.getApproverId(), instance.getApproverName(), param.getComment(), "BATCH_REMIND");
                }
            }
        }
        return true;
    }

    /**
     * 补偿审批执行数据。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean compensateExecution(WfExecutionCompensateParam param) {
        // 1. 第一段：按执行单定位运行中的审批流程，确保补偿动作只作用于有效流程。
        if (param.getExecutionId() != null) {
            WfTaskExecution execution = requireRunningExecution(param.getExecutionId());
            // 2. 第二段：查询当前执行单下尚未激活的待办实例，为补偿中心挑选首个待激活实例。
            List<WfTaskApprovalInstance> inactiveInstances = approvalInstanceMapper.selectList(new LambdaQueryWrapper<WfTaskApprovalInstance>()
                    .eq(WfTaskApprovalInstance::getExecutionId, execution.getId())
                    .eq(WfTaskApprovalInstance::getDeleted, 0)
                    .eq(WfTaskApprovalInstance::getStatus, WorkflowConstants.ApprovalInstanceStatus.PENDING)
                    .eq(WfTaskApprovalInstance::getActivated, false)
                    .orderByAsc(WfTaskApprovalInstance::getId));
            // 3. 第三段：激活首个未激活实例，并基于该实例重建待办，完成最小补偿闭环。
            if (!inactiveInstances.isEmpty()) {
                WfTaskApprovalInstance instance = inactiveInstances.get(0);
                instance.setActivated(true);
                approvalInstanceMapper.updateById(instance);
                createPendingTaskFromInstance(execution, requireCurrentExecutionDetail(execution.getId(), instance.getNodeId()), instance);
            }
        }
        return true;
    }

    /**
     * 重试超时任务。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean retryTimeoutJobs(WfExecutionCompensateParam param) {
        return getApprovalActionHandlerFactory()
                .<WfExecutionCompensateParam>getHandler(ApprovalActionHandlerType.TIMEOUT)
                .handle(param);
    }

    /**
     * 处理retrytimeoutjobsaction。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    public Boolean handleRetryTimeoutJobsAction(WfExecutionCompensateParam param) {
        // 1. 第一段：筛出当前已超时且仍处于激活待办状态的审批实例，作为超时重试候选集。
        LambdaQueryWrapper<WfTaskApprovalInstance> wrapper = new LambdaQueryWrapper<WfTaskApprovalInstance>()
                .eq(WfTaskApprovalInstance::getDeleted, 0)
                .eq(WfTaskApprovalInstance::getStatus, WorkflowConstants.ApprovalInstanceStatus.PENDING)
                .eq(WfTaskApprovalInstance::getActivated, true)
                .isNotNull(WfTaskApprovalInstance::getDeadlineTime)
                .lt(WfTaskApprovalInstance::getDeadlineTime, LocalDateTime.now());
        if (param.getExecutionId() != null) {
            wrapper.eq(WfTaskApprovalInstance::getExecutionId, param.getExecutionId());
        }
        List<WfTaskApprovalInstance> timeoutInstances = approvalInstanceMapper.selectList(wrapper);
        for (WfTaskApprovalInstance instance : timeoutInstances) {
            // 2. 第二段：逐条校验执行单仍在审批中，避免对已结束流程重复做超时补偿。
            WfTaskExecution execution = executionMapper.selectById(instance.getExecutionId());
            if (execution == null || !Objects.equals(execution.getStatus(), WorkflowConstants.ExecutionStatus.PROCESSING)) {
                continue;
            }
            // 3. 第三段：将超时实例改写为系统自动通过，并关闭对应待办与记录动作日志。
            instance.setStatus(WorkflowConstants.ApprovalInstanceStatus.APPROVED);
            instance.setActionType(WorkflowConstants.ApprovalActionType.TIMEOUT_PASS);
            instance.setComment("系统超时自动通过");
            instance.setApproveTime(LocalDateTime.now());
            approvalInstanceMapper.updateById(instance);
            WfMyTask myTask = findPendingMyTaskByInstance(instance.getId());
            if (myTask != null) {
                myTask.setStatus(1);
                myTaskMapper.updateById(myTask);
            }
            insertActionLog(execution, instance.getExecutionDetailId(), instance.getNodeId(), instance.getId(),
                    WorkflowConstants.ApprovalActionType.TIMEOUT_PASS, 0L, "系统",
                    instance.getApproverId(), instance.getApproverName(), instance.getComment(), "TIMEOUT_RETRY");
            if (Boolean.TRUE.equals(engineService.isNodeCompleted(execution.getId(), instance.getNodeId()))) {
                engineService.moveToNextNode(execution.getId(), instance.getNodeId(), 1, null);
            }
        }
        return true;
    }

    /**
     * 保存delegate。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveDelegate(WfExecutionDelegateSaveParam param) {
        return getApprovalActionHandlerFactory()
                .<WfExecutionDelegateSaveParam>getHandler(ApprovalActionHandlerType.DELEGATE)
                .handle(param);
    }

    /**
     * 处理savedelegateaction。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    public Boolean handleSaveDelegateAction(WfExecutionDelegateSaveParam param) {
        validateDelegateUsers(param);
        List<WfTaskApprovalInstance> pendingInstances = approvalInstanceMapper.selectList(new LambdaQueryWrapper<WfTaskApprovalInstance>()
                .eq(WfTaskApprovalInstance::getApproverId, param.getDelegatorUserId())
                .eq(WfTaskApprovalInstance::getDeleted, 0)
                .eq(WfTaskApprovalInstance::getStatus, WorkflowConstants.ApprovalInstanceStatus.PENDING)
                .eq(WfTaskApprovalInstance::getActivated, true));
        for (WfTaskApprovalInstance instance : pendingInstances) {
            WfTaskExecution execution = executionMapper.selectById(instance.getExecutionId());
            if (execution == null || !Objects.equals(execution.getStatus(), WorkflowConstants.ExecutionStatus.PROCESSING)) {
                continue;
            }
            closePendingTaskByInstanceId(instance.getId());
            instance.setDelegateFromUserId(param.getDelegatorUserId());
            instance.setApproverId(param.getDelegateUserId());
            instance.setApproverName(resolveUsername(param.getDelegateUserId()));
            approvalInstanceMapper.updateById(instance);
            createPendingTaskFromInstance(execution, requireCurrentExecutionDetail(execution.getId(), instance.getNodeId()), instance);
            insertActionLog(execution, instance.getExecutionDetailId(), instance.getNodeId(), instance.getId(),
                    WorkflowConstants.ApprovalActionType.DELEGATE, param.getDelegatorUserId(), resolveUsername(param.getDelegatorUserId()),
                    param.getDelegateUserId(), resolveUsername(param.getDelegateUserId()), param.getComment(), "DELEGATE");
        }
        return true;
    }

    /**
     * 取消委托审批。
     *
     * @param delegatorUserId delegator用户 ID
     * @return 是否处理成功
     */
    @Override
    public Boolean cancelDelegate(Long delegatorUserId) {
        if (delegatorUserId == null) {
            return true;
        }
        List<WfTaskApprovalInstance> delegatedInstances = approvalInstanceMapper.selectList(new LambdaQueryWrapper<WfTaskApprovalInstance>()
                .eq(WfTaskApprovalInstance::getDelegateFromUserId, delegatorUserId)
                .eq(WfTaskApprovalInstance::getDeleted, 0)
                .eq(WfTaskApprovalInstance::getStatus, WorkflowConstants.ApprovalInstanceStatus.PENDING)
                .eq(WfTaskApprovalInstance::getActivated, true));
        for (WfTaskApprovalInstance instance : delegatedInstances) {
            instance.setApproverId(delegatorUserId);
            instance.setApproverName(resolveUsername(delegatorUserId));
            instance.setDelegateFromUserId(null);
            approvalInstanceMapper.updateById(instance);
        }
        return true;
    }

    /**
     * 取消审批实例。
     *
     * @param executionId 执行 ID
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelExecution(Long executionId) {
        Long currentUserId = requireCurrentUserId();
        WfTaskExecution execution = requireRunningExecution(executionId);

        if (!Objects.equals(execution.getInitiatorId(), currentUserId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_ONLY_INITIATOR_CAN_CANCEL);
        }

        engineService.finishExecution(executionId, 3);
        log.info("撤销审批成功，executionId={}, initiatorId={}", executionId, currentUserId);
        return true;
    }

    /**
     * 获取执行详情。
     *
     * @param executionId 执行 ID
     * @return 处理结果
     */
    @Override
    public WfExecutionDTO getExecutionDetail(Long executionId) {
        WfTaskExecution execution = executionMapper.selectById(executionId);
        return execution == null ? null : convertToDTO(execution);
    }

    /**
     * 查询审批实例列表。
     *
     * @param executionId 执行 ID
     * @return 列表数据
     */
    @Override
    public List<WfApprovalInstanceDTO> listApprovalInstances(Long executionId) {
        validateExecutionExists(executionId);
        return approvalInstanceMapper.selectList(new LambdaQueryWrapper<WfTaskApprovalInstance>()
                        .eq(WfTaskApprovalInstance::getExecutionId, executionId)
                        .eq(WfTaskApprovalInstance::getDeleted, 0)
                        .orderByAsc(WfTaskApprovalInstance::getCreateTime)
                        .orderByAsc(WfTaskApprovalInstance::getId))
                .stream()
                .map(this::toApprovalInstanceDTO)
                .collect(Collectors.toList());
    }

    /**
     * 查询审批操作日志。
     *
     * @param executionId 执行 ID
     * @return 列表数据
     */
    @Override
    public List<WfApprovalActionLogDTO> listApprovalActionLogs(Long executionId) {
        validateExecutionExists(executionId);
        return approvalActionLogMapper.selectList(new LambdaQueryWrapper<WfTaskApprovalActionLog>()
                        .eq(WfTaskApprovalActionLog::getExecutionId, executionId)
                        .eq(WfTaskApprovalActionLog::getDeleted, 0)
                        .orderByDesc(WfTaskApprovalActionLog::getCreateTime)
                        .orderByDesc(WfTaskApprovalActionLog::getId))
                .stream()
                .map(this::toApprovalActionLogDTO)
                .collect(Collectors.toList());
    }

    /**
     * 分页查询我发起的审批。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    @Override
    public Page<WfExecutionDTO> pageMyInitiated(WfExecutionQueryParam param) {
        Page<WfTaskExecution> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<WfTaskExecution> wrapper = buildQueryWrapper(param);
        wrapper.eq(WfTaskExecution::getInitiatorId, requireCurrentUserId());
        wrapper.orderByDesc(WfTaskExecution::getStartTime);
        return convertToDTOPage(executionMapper.selectPage(page, wrapper));
    }

    /**
     * 分页查询我的待办审批。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    @Override
    public Page<WfExecutionDTO> pageMyPending(WfExecutionQueryParam param) {
        Long currentUserId = requireCurrentUserId();
        List<WfTaskApprovalInstance> pendingInstances = approvalInstanceMapper.selectList(
                new LambdaQueryWrapper<WfTaskApprovalInstance>()
                        .eq(WfTaskApprovalInstance::getApproverId, currentUserId)
                        .eq(WfTaskApprovalInstance::getDeleted, 0)
                        .eq(WfTaskApprovalInstance::getStatus, WorkflowConstants.ApprovalInstanceStatus.PENDING)
                        .eq(WfTaskApprovalInstance::getActivated, true)
                        .orderByDesc(WfTaskApprovalInstance::getUpdateTime)
                        .orderByDesc(WfTaskApprovalInstance::getCreateTime)
                        .orderByDesc(WfTaskApprovalInstance::getId));
        return pageByExecutionIdsFromInstances(pendingInstances, param);
    }

    /**
     * 分页查询我的已办审批。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    @Override
    public Page<WfExecutionDTO> pageMyProcessed(WfExecutionQueryParam param) {
        Long currentUserId = requireCurrentUserId();
        LocalDateTime approveBegin = parseOptionalDateTime(param.getApproveTimeBegin());
        LocalDateTime approveEnd = parseOptionalDateTime(param.getApproveTimeEnd());
        List<Integer> processedStatuses = List.of(
                WorkflowConstants.ApprovalInstanceStatus.APPROVED,
                WorkflowConstants.ApprovalInstanceStatus.REJECTED,
                WorkflowConstants.ApprovalInstanceStatus.TRANSFERRED,
                WorkflowConstants.ApprovalInstanceStatus.CLOSED
        );
        List<WfTaskApprovalInstance> processedInstances = approvalInstanceMapper.selectList(
                new LambdaQueryWrapper<WfTaskApprovalInstance>()
                        .eq(WfTaskApprovalInstance::getApproverId, currentUserId)
                        .eq(WfTaskApprovalInstance::getDeleted, 0)
                        .in(WfTaskApprovalInstance::getStatus, processedStatuses)
                        .orderByDesc(WfTaskApprovalInstance::getApproveTime)
                        .orderByDesc(WfTaskApprovalInstance::getUpdateTime)
                        .orderByDesc(WfTaskApprovalInstance::getId));
        // 1. 先按实例事实表提取“我处理过”的审批实例，并基于处理时间做窗口过滤。
        List<WfTaskApprovalInstance> matchedInstances = processedInstances.stream()
                .filter(instance -> matchesApproveTime(instance.getApproveTime(), approveBegin, approveEnd))
                .collect(Collectors.toList());
        if (matchedInstances.isEmpty()) {
            matchedInstances = loadLegacyProcessedInstances(currentUserId, approveBegin, approveEnd);
        }
        List<WfTaskApprovalInstance> delegatedInstances = approvalInstanceMapper.selectList(
                new LambdaQueryWrapper<WfTaskApprovalInstance>()
                        .eq(WfTaskApprovalInstance::getDelegateFromUserId, currentUserId)
                        .eq(WfTaskApprovalInstance::getDeleted, 0)
                        .orderByDesc(WfTaskApprovalInstance::getUpdateTime)
                        .orderByDesc(WfTaskApprovalInstance::getId));
        Set<Long> delegatedExecutionIds = delegatedInstances.stream()
                .map(WfTaskApprovalInstance::getExecutionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        // 2. 再补上“我发起了委托但实例已转给别人处理”的场景，避免委托人丢失已办可见性。
        List<WfTaskApprovalInstance> delegateActionInstances = delegatedExecutionIds.isEmpty()
                ? Collections.emptyList()
                : approvalActionLogMapper.selectList(new LambdaQueryWrapper<WfTaskApprovalActionLog>()
                .eq(WfTaskApprovalActionLog::getOperatorId, currentUserId)
                .eq(WfTaskApprovalActionLog::getActionType, WorkflowConstants.ApprovalActionType.DELEGATE)
                .eq(WfTaskApprovalActionLog::getDeleted, 0)
                .in(WfTaskApprovalActionLog::getExecutionId, delegatedExecutionIds)
                .orderByDesc(WfTaskApprovalActionLog::getCreateTime)
                .orderByDesc(WfTaskApprovalActionLog::getId)).stream()
                .filter(actionLog -> matchesApproveTime(actionLog.getCreateTime(), approveBegin, approveEnd))
                .map(actionLog -> {
                    WfTaskApprovalInstance instance = new WfTaskApprovalInstance();
                    instance.setExecutionId(actionLog.getExecutionId());
                    instance.setApproveTime(actionLog.getCreateTime());
                    instance.setUpdateTime(actionLog.getCreateTime());
                    return instance;
                })
                .collect(Collectors.toList());
        if (!delegateActionInstances.isEmpty()) {
            // 3. 合并委托动作产生的已办候选集，并继续沿用实例驱动的执行单聚合展示。
            matchedInstances.addAll(delegateActionInstances);
            matchedInstances.sort((left, right) -> compareProcessedInstanceTime(right, left));
        }
        return pageByExecutionIdsFromInstances(matchedInstances, param);
    }

    private List<WfTaskApprovalInstance> loadLegacyProcessedInstances(Long currentUserId,
                                                                      LocalDateTime approveBegin,
                                                                      LocalDateTime approveEnd) {
        List<WfTaskExecutionApprover> approverRecords = executionApproverMapper.selectList(
                new LambdaQueryWrapper<WfTaskExecutionApprover>()
                        .orderByDesc(WfTaskExecutionApprover::getUpdateTime)
                        .orderByDesc(WfTaskExecutionApprover::getCreateTime)
                        .orderByDesc(WfTaskExecutionApprover::getId));
        return approverRecords.stream()
                .filter(record -> matchesMyProcessed(record.getApproverDetail(), currentUserId, approveBegin, approveEnd))
                .map(record -> {
                    WfTaskApprovalInstance instance = new WfTaskApprovalInstance();
                    instance.setExecutionId(record.getExecutionId());
                    instance.setNodeId(record.getNodeId());
                    instance.setExecutionDetailId(record.getExecutionDetailId());
                    instance.setApproveTime(resolveLegacyApproveTime(record.getApproverDetail(), currentUserId));
                    instance.setCreateTime(record.getCreateTime());
                    instance.setUpdateTime(record.getUpdateTime());
                    return instance;
                })
                .sorted((left, right) -> compareProcessedInstanceTime(right, left))
                .collect(Collectors.toList());
    }

    private LocalDateTime resolveLegacyApproveTime(String approverDetailJson, Long userId) {
        if (!StringUtils.hasText(approverDetailJson) || userId == null) {
            return null;
        }
        JSONArray approverDetails = JSON.parseArray(approverDetailJson);
        if (approverDetails == null || approverDetails.isEmpty()) {
            return null;
        }
        for (int i = 0; i < approverDetails.size(); i++) {
            JSONObject detail = approverDetails.getJSONObject(i);
            if (detail == null || !Objects.equals(detail.getLong("approverId"), userId)) {
                continue;
            }
            LocalDateTime approveTime = parseOptionalDateTime(detail.getString("approveTime"));
            if (approveTime != null) {
                return approveTime;
            }
        }
        return null;
    }

    /**
     * 分页查询抄送我的审批。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    @Override
    public Page<WfExecutionDTO> pageMyCc(WfExecutionQueryParam param) {
        Long currentUserId = requireCurrentUserId();
        List<WfTaskApprovalInstance> currentInstances = approvalInstanceMapper.selectList(
                new LambdaQueryWrapper<WfTaskApprovalInstance>()
                        .eq(WfTaskApprovalInstance::getApproverId, currentUserId)
                        .eq(WfTaskApprovalInstance::getDeleted, 0)
                        .eq(WfTaskApprovalInstance::getActivated, true)
                        .orderByDesc(WfTaskApprovalInstance::getUpdateTime)
                        .orderByDesc(WfTaskApprovalInstance::getCreateTime)
                        .orderByDesc(WfTaskApprovalInstance::getId));

        List<WfTaskApprovalInstance> ccInstances = currentInstances.stream()
                .filter(instance -> {
                    WfTaskNodeConfig node = nodeConfigMapper.selectById(instance.getNodeId());
                    return node != null
                            && !Objects.equals(node.getDeleted(), 1)
                            && Objects.equals(node.getApproveType(), WorkflowConstants.ApproveType.COPY);
                })
                .collect(Collectors.toList());
        return pageByExecutionIdsFromInstances(ccInstances, param);
    }

    /**
     * 分页查询补偿中心列表。
     * <p>
     * 1. 先从审批实例表中筛出“未激活待办实例”与“已超时激活实例”，建立治理候选集。
     * 2. 再按执行单维度去重并复用执行单聚合展示逻辑，保持与列表页展示结构一致。
     * 3. 最后沿用执行单查询条件过滤，便于补偿中心按任务名称、编码和状态检索。
     * </p>
     *
     * @param param 查询参数
     * @return 补偿中心分页结果
     */
    @Override
    public Page<WfExecutionDTO> pageCompensationCenter(WfExecutionQueryParam param) {
        LocalDateTime now = LocalDateTime.now();

        // 1. 第一段：查询需要人工介入的未激活待办实例，为后续补偿激活提供候选数据。
        List<WfTaskApprovalInstance> inactiveInstances = approvalInstanceMapper.selectList(
                new LambdaQueryWrapper<WfTaskApprovalInstance>()
                        .eq(WfTaskApprovalInstance::getDeleted, 0)
                        .eq(WfTaskApprovalInstance::getStatus, WorkflowConstants.ApprovalInstanceStatus.PENDING)
                        .eq(WfTaskApprovalInstance::getActivated, false)
                        .orderByDesc(WfTaskApprovalInstance::getUpdateTime)
                        .orderByDesc(WfTaskApprovalInstance::getCreateTime)
                        .orderByDesc(WfTaskApprovalInstance::getId));

        // 2. 第二段：查询已超时但仍处于激活待办的实例，为超时重试入口提供候选数据。
        List<WfTaskApprovalInstance> timeoutInstances = approvalInstanceMapper.selectList(
                new LambdaQueryWrapper<WfTaskApprovalInstance>()
                        .eq(WfTaskApprovalInstance::getDeleted, 0)
                        .eq(WfTaskApprovalInstance::getStatus, WorkflowConstants.ApprovalInstanceStatus.PENDING)
                        .eq(WfTaskApprovalInstance::getActivated, true)
                        .isNotNull(WfTaskApprovalInstance::getDeadlineTime)
                        .lt(WfTaskApprovalInstance::getDeadlineTime, now)
                        .orderByDesc(WfTaskApprovalInstance::getDeadlineTime)
                        .orderByDesc(WfTaskApprovalInstance::getUpdateTime)
                        .orderByDesc(WfTaskApprovalInstance::getId));

        // 3. 第三段：合并实例并按治理优先级排序，保证补偿中心优先看到超时或最新异常的执行单。
        List<WfTaskApprovalInstance> compensationInstances = new ArrayList<>();
        compensationInstances.addAll(timeoutInstances);
        compensationInstances.addAll(inactiveInstances);
        compensationInstances.sort((left, right) -> compareProcessedInstanceTime(right, left));
        return pageByExecutionIdsFromInstances(compensationInstances, param);
    }

    /**
     * 加载审批看板汇总数据。
     *
     * @return 处理结果
     */
    @Override
    public WfDashboardSummaryVO loadDashboardSummary() {
        WfDashboardSummaryVO vo = new WfDashboardSummaryVO();

        WfExecutionQueryParam limitParam = new WfExecutionQueryParam();
        limitParam.setPageNum(1);
        limitParam.setPageSize(DASHBOARD_LIST_LIMIT);

        vo.setPending(pageMyPending(limitParam).getRecords());

        LocalDate yesterday = LocalDate.now(BUSINESS_ZONE).minusDays(1);
        LocalDateTime dayStart = yesterday.atStartOfDay();
        LocalDateTime dayEnd = yesterday.atTime(23, 59, 59);

        WfExecutionQueryParam processedParam = new WfExecutionQueryParam();
        processedParam.setPageNum(1);
        processedParam.setPageSize(DASHBOARD_LIST_LIMIT);
        processedParam.setApproveTimeBegin(dayStart.format(APPROVE_TIME_FORMATTER));
        processedParam.setApproveTimeEnd(dayEnd.format(APPROVE_TIME_FORMATTER));
        vo.setYesterdayProcessed(pageMyProcessed(processedParam).getRecords());

        vo.setCc(pageMyCc(limitParam).getRecords());
        return vo;
    }

    /**
     * 加载审批看板统计数据。
     *
     * @return 处理结果
     */
    @Override
    public WfDashboardAnalyticsVO loadDashboardAnalytics() {
        Long tenantId = requireCurrentTenantId();
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate startDate = today.minusDays(6);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay();

        List<WfDashboardWeeklyResultDTO> rawWeeklyResults = executionMapper
                .selectDashboardWeeklyResults(tenantId, startTime, endTime);
        Map<String, WfDashboardWeeklyResultDTO> weeklyResultMap = new HashMap<>();
        for (WfDashboardWeeklyResultDTO item : rawWeeklyResults) {
            if (item != null && StringUtils.hasText(item.getDate())) {
                weeklyResultMap.put(item.getDate(), item);
            }
        }

        List<WfDashboardWeeklyResultDTO> weeklyResults = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String date = startDate.plusDays(i).format(DASHBOARD_DATE_FORMATTER);
            WfDashboardWeeklyResultDTO source = weeklyResultMap.get(date);

            WfDashboardWeeklyResultDTO result = new WfDashboardWeeklyResultDTO();
            result.setDate(date);
            result.setApprovedCount(source == null || source.getApprovedCount() == null ? 0L : source.getApprovedCount());
            result.setRejectedCount(source == null || source.getRejectedCount() == null ? 0L : source.getRejectedCount());
            weeklyResults.add(result);
        }

        List<WfDashboardUserShareDTO> userShares = executionMapper.selectDashboardUserShares(tenantId);

        WfDashboardAnalyticsVO analytics = new WfDashboardAnalyticsVO();
        analytics.setWeeklyResults(weeklyResults);
        analytics.setUserShares(userShares == null ? Collections.emptyList() : userShares);
        return analytics;
    }

    private Page<WfExecutionDTO> pageByExecutionIds(List<Long> executionIds, WfExecutionQueryParam param) {
        Page<WfExecutionDTO> page = new Page<>(param.getPageNum(), param.getPageSize());
        if (executionIds == null || executionIds.isEmpty()) {
            page.setRecords(List.of());
            page.setTotal(0);
            return page;
        }

        List<Long> orderedIds = executionIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<WfTaskExecution> executions = executionMapper.selectList(new LambdaQueryWrapper<WfTaskExecution>()
                .in(WfTaskExecution::getId, orderedIds)
                .eq(WfTaskExecution::getDeleted, 0));

        Map<Long, WfTaskExecution> executionMap = executions.stream()
                .collect(Collectors.toMap(WfTaskExecution::getId, item -> item));

        List<WfExecutionDTO> matchedRecords = orderedIds.stream()
                .map(executionMap::get)
                .filter(Objects::nonNull)
                .filter(execution -> matchesQuery(execution, param))
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        int total = matchedRecords.size();
        int fromIndex = Math.max((param.getPageNum() - 1) * param.getPageSize(), 0);
        int toIndex = Math.min(fromIndex + param.getPageSize(), total);

        page.setTotal(total);
        if (fromIndex >= total) {
            page.setRecords(List.of());
            return page;
        }

        page.setRecords(matchedRecords.subList(fromIndex, toIndex));
        return page;
    }

    private Page<WfExecutionDTO> pageByExecutionIdsFromInstances(List<WfTaskApprovalInstance> instances, WfExecutionQueryParam param) {
        if (instances == null || instances.isEmpty()) {
            Page<WfExecutionDTO> page = new Page<>(param.getPageNum(), param.getPageSize());
            page.setRecords(List.of());
            page.setTotal(0);
            return page;
        }
        List<Long> orderedExecutionIds = instances.stream()
                .map(WfTaskApprovalInstance::getExecutionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return pageByExecutionIds(orderedExecutionIds, param);
    }

    private boolean matchesQuery(WfTaskExecution execution, WfExecutionQueryParam param) {
        if (StringUtils.hasText(param.getTaskName())
                && !containsIgnoreCase(execution.getTaskName(), param.getTaskName())) {
            return false;
        }
        if (StringUtils.hasText(param.getTaskCode())
                && !containsIgnoreCase(execution.getTaskCode(), param.getTaskCode())) {
            return false;
        }
        return param.getStatus() == null || Objects.equals(execution.getStatus(), param.getStatus());
    }

    private boolean containsIgnoreCase(String source, String target) {
        if (!StringUtils.hasText(source) || !StringUtils.hasText(target)) {
            return false;
        }
        return source.toLowerCase().contains(target.toLowerCase());
    }

    private boolean matchesApproveTime(LocalDateTime approveTime, LocalDateTime approveBegin, LocalDateTime approveEnd) {
        if (approveBegin == null && approveEnd == null) {
            return true;
        }
        if (approveTime == null) {
            return false;
        }
        if (approveBegin != null && approveTime.isBefore(approveBegin)) {
            return false;
        }
        return approveEnd == null || !approveTime.isAfter(approveEnd);
    }

    private int compareProcessedInstanceTime(WfTaskApprovalInstance left, WfTaskApprovalInstance right) {
        LocalDateTime leftTime = left == null ? null : firstNonNull(left.getApproveTime(), left.getUpdateTime(), left.getCreateTime());
        LocalDateTime rightTime = right == null ? null : firstNonNull(right.getApproveTime(), right.getUpdateTime(), right.getCreateTime());
        if (leftTime == null && rightTime == null) {
            return 0;
        }
        if (leftTime == null) {
            return -1;
        }
        if (rightTime == null) {
            return 1;
        }
        return leftTime.compareTo(rightTime);
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private WfTaskExecution requireRunningExecution(Long executionId) {
        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_EXECUTION_NOT_FOUND);
        }
        if (!Objects.equals(execution.getStatus(), 1)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_EXECUTION_NOT_RUNNING);
        }
        return execution;
    }

    private WfMyTask requirePendingTask(Long executionId, Long nodeId, Long approverId) {
        List<WfMyTask> myTasks = myTaskMapper.selectList(new LambdaQueryWrapper<WfMyTask>()
                .eq(WfMyTask::getExecutionId, executionId)
                .eq(WfMyTask::getNodeId, nodeId)
                .eq(WfMyTask::getStatus, 0)
                .orderByDesc(WfMyTask::getId));

        WfTaskApprovalInstance pendingInstance = findCurrentPendingInstance(executionId, nodeId, approverId);
        if (pendingInstance != null) {
            WfTaskApprovalInstance finalPendingInstance = pendingInstance;
            WfMyTask instanceTask = myTasks.stream()
                    .filter(task -> Objects.equals(task.getApprovalInstanceId(), finalPendingInstance.getId()))
                    .findFirst()
                    .orElse(null);
            if (instanceTask != null) {
                return instanceTask;
            }
            WfTaskExecution execution = executionMapper.selectById(executionId);
            WfTaskExecutionDetail currentDetail = requireCurrentExecutionDetail(executionId, nodeId);
            if (execution != null) {
                createPendingTaskFromInstance(execution, currentDetail, pendingInstance);
                WfMyTask repairedTask = findPendingMyTaskByInstance(pendingInstance.getId());
                if (repairedTask != null) {
                    log.warn("审批待办缺失，已自动补齐，executionId={}, nodeId={}, approvalInstanceId={}, approverId={}",
                            executionId, nodeId, pendingInstance.getId(), approverId);
                    return repairedTask;
                }
            }
        }

        return myTasks.stream()
                .filter(task -> matchesPendingTask(task, approverId, pendingInstance))
                .findFirst()
                .orElseThrow(() -> new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_USER_NO_PENDING_TASK));
    }

    private WfTaskApprovalInstance findCurrentPendingInstance(Long executionId, Long nodeId, Long approverId) {
        if (approverId == null) {
            return null;
        }
        return approvalInstanceMapper.selectOne(new LambdaQueryWrapper<WfTaskApprovalInstance>()
                .eq(WfTaskApprovalInstance::getExecutionId, executionId)
                .eq(WfTaskApprovalInstance::getNodeId, nodeId)
                .eq(WfTaskApprovalInstance::getApproverId, approverId)
                .eq(WfTaskApprovalInstance::getDeleted, 0)
                .eq(WfTaskApprovalInstance::getStatus, WorkflowConstants.ApprovalInstanceStatus.PENDING)
                .eq(WfTaskApprovalInstance::getActivated, true)
                .orderByDesc(WfTaskApprovalInstance::getId)
                .last("LIMIT 1"));
    }

    private boolean matchesPendingTask(WfMyTask task, Long approverId, WfTaskApprovalInstance pendingInstance) {
        if (pendingInstance != null) {
            return Objects.equals(task.getApprovalInstanceId(), pendingInstance.getId())
                    || (task.getApprovalInstanceId() == null && containsApprover(task.getApproverIds(), approverId));
        }
        return Objects.equals(task.getApproverId(), approverId) || containsApprover(task.getApproverIds(), approverId);
    }

    private WfTaskExecutionDetail requireCurrentExecutionDetail(Long executionId, Long nodeId) {
        WfTaskExecutionDetail detail = executionDetailMapper.selectOne(new LambdaQueryWrapper<WfTaskExecutionDetail>()
                .eq(WfTaskExecutionDetail::getExecutionId, executionId)
                .eq(WfTaskExecutionDetail::getNodeId, nodeId)
                .orderByDesc(WfTaskExecutionDetail::getId)
                .last("LIMIT 1"));

        if (detail == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_EXECUTION_DETAIL_NOT_FOUND);
        }
        return detail;
    }

    private void saveApprovalRecord(WfTaskExecutionDetail detail,
                                    WfTaskExecution execution,
                                    Long approverId,
                                    String approverName,
                                    Integer approveStatus,
                                    String comment,
                                    Integer rejectType,
                                    LocalDateTime approveTime) {
        WfTaskExecutionApprover approverRecord = executionApproverMapper.selectOne(
                new LambdaQueryWrapper<WfTaskExecutionApprover>()
                        .eq(WfTaskExecutionApprover::getExecutionId, execution.getId())
                        .eq(WfTaskExecutionApprover::getNodeId, detail.getNodeId())
                        .eq(WfTaskExecutionApprover::getExecutionDetailId, detail.getId())
                        .orderByDesc(WfTaskExecutionApprover::getId)
                        .last("LIMIT 1"));

        boolean isNew = approverRecord == null;
        if (isNew) {
            approverRecord = new WfTaskExecutionApprover();
            approverRecord.setExecutionId(execution.getId());
            approverRecord.setExecutionDetailId(detail.getId());
            approverRecord.setNodeId(detail.getNodeId());
            approverRecord.setTenantId(execution.getTenantId());
        }

        JSONArray approverDetails = StringUtils.hasText(approverRecord.getApproverDetail())
                ? JSON.parseArray(approverRecord.getApproverDetail())
                : new JSONArray();

        JSONObject approverDetail = new JSONObject();
        approverDetail.put("approverId", approverId);
        approverDetail.put("approverName", approverName);
        approverDetail.put("approveStatus", approveStatus);
        approverDetail.put("approveTime", approveTime.format(APPROVE_TIME_FORMATTER));
        approverDetail.put("comment", comment);
        approverDetails.add(approverDetail);

        approverRecord.setApproverDetail(approverDetails.toJSONString());
        approverRecord.setRejectType(rejectType);

        if (isNew) {
            executionApproverMapper.insert(approverRecord);
        } else {
            executionApproverMapper.updateById(approverRecord);
        }
    }

    private boolean containsApprover(String approverIdsJson, Long approverId) {
        if (!StringUtils.hasText(approverIdsJson) || approverId == null) {
            return false;
        }

        JSONArray approverIds = JSON.parseArray(approverIdsJson);
        if (approverIds == null || approverIds.isEmpty()) {
            return false;
        }

        for (int i = 0; i < approverIds.size(); i++) {
            if (Objects.equals(approverId, approverIds.getLong(i))) {
                return true;
            }
        }
        return false;
    }

    private WfTaskApprovalInstance resolveApprovalInstance(WfMyTask pendingTask, Long approvalInstanceId, Long approverId) {
        Long targetInstanceId = approvalInstanceId != null ? approvalInstanceId : pendingTask.getApprovalInstanceId();
        WfTaskApprovalInstance instance = targetInstanceId == null
                ? findCurrentPendingInstance(pendingTask.getExecutionId(), pendingTask.getNodeId(), approverId)
                : approvalInstanceMapper.selectById(targetInstanceId);
        if (instance == null && targetInstanceId == null) {
            return null;
        }
        if (instance == null || !Objects.equals(instance.getApproverId(), approverId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_INSTANCE_NOT_FOUND_OR_NO_PERMISSION);
        }
        return instance;
    }

    private void updateAfterInstanceAction(WfTaskExecution execution,
                                           WfTaskExecutionDetail currentDetail,
                                           WfMyTask pendingTask,
                                           WfTaskApprovalInstance approvalInstance,
                                           Integer instanceStatus,
                                           Integer actionType,
                                           String comment,
                                           String approverName,
                                           LocalDateTime approveTime) {
        currentDetail.setCurrentStatus(Objects.equals(instanceStatus, WorkflowConstants.ApprovalInstanceStatus.REJECTED)
                ? WorkflowConstants.NodeStatus.REJECTED
                : WorkflowConstants.NodeStatus.APPROVED);
        executionDetailMapper.updateById(currentDetail);

        pendingTask.setStatus(1);
        myTaskMapper.updateById(pendingTask);

        if (approvalInstance == null) {
            return;
        }
        approvalInstance.setStatus(instanceStatus);
        approvalInstance.setActionType(actionType);
        approvalInstance.setComment(comment);
        approvalInstance.setApproveTime(approveTime);
        approvalInstanceMapper.updateById(approvalInstance);
        closePendingMyTaskByInstance(approvalInstance.getId());
        insertApprovalActionLog(execution, currentDetail, approvalInstance, actionType, approverName, comment);
        if (Objects.equals(instanceStatus, WorkflowConstants.ApprovalInstanceStatus.APPROVED)) {
            activateNextSequentialInstanceIfNecessary(execution, currentDetail);
        } else {
            closeOtherPendingInstances(execution.getId(), currentDetail.getId(), currentDetail.getNodeId(), approvalInstance.getId());
        }
    }

    private void closePendingMyTaskByInstance(Long approvalInstanceId) {
        if (approvalInstanceId == null) {
            return;
        }
        List<WfMyTask> myTasks = myTaskMapper.selectList(new LambdaQueryWrapper<WfMyTask>()
                .eq(WfMyTask::getApprovalInstanceId, approvalInstanceId)
                .eq(WfMyTask::getStatus, 0));
        for (WfMyTask myTask : myTasks) {
            myTask.setStatus(1);
            myTaskMapper.updateById(myTask);
        }
    }

    private void closeOtherPendingInstancesIfOrSign(WfTaskExecutionDetail currentDetail,
                                                    WfTaskApprovalInstance approvalInstance,
                                                    Integer instanceStatus) {
        if (approvalInstance == null
                || !Objects.equals(instanceStatus, WorkflowConstants.ApprovalInstanceStatus.APPROVED)) {
            return;
        }
        WfTaskNodeConfig node = nodeConfigMapper.selectById(currentDetail.getNodeId());
        if (node == null || !Objects.equals(node.getApproveType(), WorkflowConstants.ApproveType.OR_SIGN)) {
            return;
        }
        closeOtherPendingInstances(
                approvalInstance.getExecutionId(),
                currentDetail.getId(),
                currentDetail.getNodeId(),
                approvalInstance.getId());
    }

    private void activateNextSequentialInstanceIfNecessary(WfTaskExecution execution, WfTaskExecutionDetail currentDetail) {
        WfTaskNodeConfig node = nodeConfigMapper.selectById(currentDetail.getNodeId());
        if (node == null || !Objects.equals(node.getApproveType(), WorkflowConstants.ApproveType.SEQUENTIAL)) {
            return;
        }
        WfTaskApprovalInstance nextInstance = approvalInstanceMapper.selectOne(new LambdaQueryWrapper<WfTaskApprovalInstance>()
                .eq(WfTaskApprovalInstance::getExecutionId, execution.getId())
                .eq(WfTaskApprovalInstance::getExecutionDetailId, currentDetail.getId())
                .eq(WfTaskApprovalInstance::getNodeId, currentDetail.getNodeId())
                .eq(WfTaskApprovalInstance::getDeleted, 0)
                .eq(WfTaskApprovalInstance::getActivated, false)
                .eq(WfTaskApprovalInstance::getStatus, WorkflowConstants.ApprovalInstanceStatus.PENDING)
                .orderByAsc(WfTaskApprovalInstance::getId)
                .last("LIMIT 1"));
        if (nextInstance == null) {
            return;
        }
        nextInstance.setActivated(true);
        approvalInstanceMapper.updateById(nextInstance);

        WfMyTask nextTask = new WfMyTask();
        nextTask.setExecutionId(execution.getId());
        nextTask.setTaskName(execution.getTaskName());
        nextTask.setNodeId(currentDetail.getNodeId());
        nextTask.setNodeName(currentDetail.getNodeName());
        nextTask.setApprovalInstanceId(nextInstance.getId());
        nextTask.setExecutionDetailId(currentDetail.getId());
        nextTask.setApproverId(nextInstance.getApproverId());
        nextTask.setApproverIds(JSON.toJSONString(Collections.singletonList(nextInstance.getApproverId())));
        nextTask.setStatus(0);
        nextTask.setTenantId(execution.getTenantId());
        nextTask.setDeadlineTime(nextInstance.getDeadlineTime());
        myTaskMapper.insert(nextTask);
    }

    private void closeOtherPendingInstances(Long executionId, Long executionDetailId, Long nodeId, Long excludeInstanceId) {
        List<WfTaskApprovalInstance> otherInstances = approvalInstanceMapper.selectList(new LambdaQueryWrapper<WfTaskApprovalInstance>()
                .eq(WfTaskApprovalInstance::getExecutionId, executionId)
                .eq(WfTaskApprovalInstance::getExecutionDetailId, executionDetailId)
                .eq(WfTaskApprovalInstance::getNodeId, nodeId)
                .eq(WfTaskApprovalInstance::getDeleted, 0)
                .eq(WfTaskApprovalInstance::getStatus, WorkflowConstants.ApprovalInstanceStatus.PENDING));
        for (WfTaskApprovalInstance instance : otherInstances) {
            if (Objects.equals(instance.getId(), excludeInstanceId)) {
                continue;
            }
            instance.setStatus(WorkflowConstants.ApprovalInstanceStatus.CLOSED);
            instance.setActionType(WorkflowConstants.ApprovalActionType.SYSTEM_CLOSE);
            approvalInstanceMapper.updateById(instance);
        }
        List<WfMyTask> myTasks = myTaskMapper.selectList(new LambdaQueryWrapper<WfMyTask>()
                .eq(WfMyTask::getExecutionId, executionId)
                .eq(WfMyTask::getNodeId, nodeId)
                .eq(WfMyTask::getStatus, 0));
        for (WfMyTask myTask : myTasks) {
            if (Objects.equals(myTask.getApprovalInstanceId(), excludeInstanceId)) {
                continue;
            }
            if (myTask.getExecutionDetailId() != null && !Objects.equals(myTask.getExecutionDetailId(), executionDetailId)) {
                continue;
            }
            myTask.setStatus(1);
            myTaskMapper.updateById(myTask);
        }
    }

    private void insertApprovalActionLog(WfTaskExecution execution,
                                         WfTaskExecutionDetail currentDetail,
                                         WfTaskApprovalInstance approvalInstance,
                                         Integer actionType,
                                         String approverName,
                                         String comment) {
        WfTaskApprovalActionLog actionLog = new WfTaskApprovalActionLog();
        actionLog.setExecutionId(execution.getId());
        actionLog.setExecutionDetailId(currentDetail.getId());
        actionLog.setNodeId(currentDetail.getNodeId());
        actionLog.setApprovalInstanceId(approvalInstance.getId());
        actionLog.setActionType(actionType);
        actionLog.setOperatorId(approvalInstance.getApproverId());
        actionLog.setOperatorName(approverName);
        actionLog.setTargetUserId(approvalInstance.getApproverId());
        actionLog.setTargetUserName(approvalInstance.getApproverName());
        actionLog.setActionComment(comment);
        actionLog.setActionSnapshot(JSON.toJSONString(approvalInstance));
        actionLog.setTenantId(execution.getTenantId());
        actionLog.setDeleted(0);
        approvalActionLogMapper.insert(actionLog);
    }

    private WfTaskApprovalInstance requirePendingApprovalInstance(Long approvalInstanceId, Long executionId) {
        if (approvalInstanceId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_APPROVAL_INSTANCE_REQUIRED);
        }
        WfTaskApprovalInstance instance = approvalInstanceMapper.selectById(approvalInstanceId);
        if (instance == null || !Objects.equals(instance.getExecutionId(), executionId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_INSTANCE_NOT_FOUND_OR_NO_PERMISSION);
        }
        if (!Objects.equals(instance.getStatus(), WorkflowConstants.ApprovalInstanceStatus.PENDING)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_INSTANCE_ALREADY_PROCESSED);
        }
        return instance;
    }

    private WfMyTask findPendingMyTaskByInstance(Long approvalInstanceId) {
        if (approvalInstanceId == null) {
            return null;
        }
        return myTaskMapper.selectOne(new LambdaQueryWrapper<WfMyTask>()
                .eq(WfMyTask::getApprovalInstanceId, approvalInstanceId)
                .eq(WfMyTask::getStatus, 0)
                .orderByDesc(WfMyTask::getId)
                .last("LIMIT 1"));
    }

    private void closePendingTaskByInstanceId(Long approvalInstanceId) {
        WfMyTask currentTask = findPendingMyTaskByInstance(approvalInstanceId);
        if (currentTask == null) {
            return;
        }
        currentTask.setStatus(1);
        myTaskMapper.updateById(currentTask);
    }

    private void createPendingTaskFromInstance(WfTaskExecution execution,
                                               WfTaskExecutionDetail detail,
                                               WfTaskApprovalInstance instance) {
        WfMyTask newTask = new WfMyTask();
        newTask.setExecutionId(execution.getId());
        newTask.setTaskName(execution.getTaskName());
        newTask.setNodeId(instance.getNodeId());
        newTask.setNodeName(detail.getNodeName());
        newTask.setApprovalInstanceId(instance.getId());
        newTask.setExecutionDetailId(instance.getExecutionDetailId());
        newTask.setApproverId(instance.getApproverId());
        newTask.setApproverIds(JSON.toJSONString(Collections.singletonList(instance.getApproverId())));
        newTask.setStatus(0);
        newTask.setTenantId(execution.getTenantId());
        newTask.setDeadlineTime(instance.getDeadlineTime());
        myTaskMapper.insert(newTask);
    }

    private WfTaskApprovalInstance buildPendingInstanceFromCurrent(WfTaskApprovalInstance currentInstance, Long targetApproverId) {
        WfTaskApprovalInstance targetInstance = new WfTaskApprovalInstance();
        BeanUtils.copyProperties(currentInstance, targetInstance, "id", "createTime", "updateTime");
        targetInstance.setApproverId(targetApproverId);
        targetInstance.setApproverName(resolveUsername(targetApproverId));
        targetInstance.setStatus(WorkflowConstants.ApprovalInstanceStatus.PENDING);
        targetInstance.setActionType(null);
        targetInstance.setComment(null);
        targetInstance.setApproveTime(null);
        targetInstance.setActivated(true);
        targetInstance.setDelegateFromUserId(null);
        targetInstance.setDeleted(0);
        return targetInstance;
    }

    private void insertActionLog(WfTaskExecution execution,
                                 Long executionDetailId,
                                 Long nodeId,
                                 Long approvalInstanceId,
                                 Integer actionType,
                                 Long operatorId,
                                 String operatorName,
                                 Long targetUserId,
                                 String targetUserName,
                                 String actionComment,
                                 String actionSnapshot) {
        WfTaskApprovalActionLog actionLog = new WfTaskApprovalActionLog();
        actionLog.setExecutionId(execution.getId());
        actionLog.setExecutionDetailId(executionDetailId);
        actionLog.setNodeId(nodeId);
        actionLog.setApprovalInstanceId(approvalInstanceId);
        actionLog.setActionType(actionType);
        actionLog.setOperatorId(operatorId);
        actionLog.setOperatorName(operatorName);
        actionLog.setTargetUserId(targetUserId);
        actionLog.setTargetUserName(targetUserName);
        actionLog.setActionComment(actionComment);
        actionLog.setActionSnapshot(actionSnapshot);
        actionLog.setTenantId(execution.getTenantId());
        actionLog.setDeleted(0);
        approvalActionLogMapper.insert(actionLog);
    }

    private void validateDelegateUsers(WfExecutionDelegateSaveParam param) {
        if (param.getDelegatorUserId() == null || param.getDelegateUserId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_DELEGATOR_AND_DELEGATEE_REQUIRED);
        }
        if (Objects.equals(param.getDelegatorUserId(), param.getDelegateUserId())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_DELEGATEE_SAME_AS_DELEGATOR);
        }
    }

    /**
     * 判断当前用户在审批明细 JSON 中是否存在符合条件的处理记录。
     * <p>
     * 当 {@code approveBegin}、{@code approveEnd} 均为空时，只要存在该用户的任意一条审批记录即匹配；
     * 否则按 {@code approveTime}（格式 {@link #APPROVE_TIME_FORMATTER}）与区间做交集判断（闭区间）。
     * </p>
     *
     * @param approverDetailJson 审批人明细 JSON 数组字符串
     * @param userId               当前用户 ID
     * @param approveBegin         审批操作时间下限，可为空
     * @param approveEnd           审批操作时间上限，可为空
     * @return 是否匹配
     */
    private boolean matchesMyProcessed(String approverDetailJson,
                                       Long userId,
                                       LocalDateTime approveBegin,
                                       LocalDateTime approveEnd) {
        if (!StringUtils.hasText(approverDetailJson) || userId == null) {
            return false;
        }

        JSONArray approverDetails = JSON.parseArray(approverDetailJson);
        if (approverDetails == null || approverDetails.isEmpty()) {
            return false;
        }

        boolean filterByTime = approveBegin != null || approveEnd != null;

        for (int i = 0; i < approverDetails.size(); i++) {
            JSONObject detail = approverDetails.getJSONObject(i);
            if (detail == null || !Objects.equals(detail.getLong("approverId"), userId)) {
                continue;
            }
            if (!filterByTime) {
                return true;
            }
            LocalDateTime approveTime = parseOptionalDateTime(detail.getString("approveTime"));
            if (approveTime == null) {
                continue;
            }
            if (approveBegin != null && approveTime.isBefore(approveBegin)) {
                continue;
            }
            if (approveEnd != null && approveTime.isAfter(approveEnd)) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * 解析可选的日期时间字符串。
     *
     * @param value 字符串，格式 {@code yyyy-MM-dd HH:mm:ss}
     * @return 解析成功返回时间，否则返回 {@code null}
     */
    private LocalDateTime parseOptionalDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), APPROVE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("解析审批时间失败: {}", value, e);
            return null;
        }
    }

    private LambdaQueryWrapper<WfTaskExecution> buildQueryWrapper(WfExecutionQueryParam param) {
        LambdaQueryWrapper<WfTaskExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskExecution::getDeleted, 0);

        if (StringUtils.hasText(param.getTaskName())) {
            wrapper.like(WfTaskExecution::getTaskName, param.getTaskName());
        }
        if (StringUtils.hasText(param.getTaskCode())) {
            wrapper.like(WfTaskExecution::getTaskCode, param.getTaskCode());
        }
        if (param.getStatus() != null) {
            wrapper.eq(WfTaskExecution::getStatus, param.getStatus());
        }

        return wrapper;
    }

    private WfExecutionDTO convertToDTO(WfTaskExecution execution) {
        WfExecutionDTO dto = new WfExecutionDTO();
        BeanUtils.copyProperties(execution, dto);
        fillExecutionFormMeta(dto, execution.getTaskConfigId());
        List<WfApprovalInstanceDTO> instances = loadApprovalInstancesQuietly(execution.getId());
        List<WfApprovalActionLogDTO> actionLogs = loadApprovalActionLogsQuietly(execution.getId());
        // 1. 先补齐执行单详情抽屉所需的实例轨迹和动作日志，减少前端重复拼装。
        dto.setCurrentApprovalInstances(instances);
        dto.setApprovalActionLogs(actionLogs);
        // 2. 再汇总治理中心与仪表盘会使用的运行态统计字段。
        dto.setActiveInstanceCount((int) instances.stream()
                .filter(item -> Boolean.TRUE.equals(item.getActivated())
                        && Objects.equals(item.getStatus(), WorkflowConstants.ApprovalInstanceStatus.PENDING))
                .count());
        dto.setTimeoutFlag(instances.stream().anyMatch(item -> item.getDeadlineTime() != null
                && Objects.equals(item.getStatus(), WorkflowConstants.ApprovalInstanceStatus.PENDING)
                && item.getDeadlineTime().isBefore(LocalDateTime.now())));
        dto.setDelegated(instances.stream().anyMatch(item -> item.getDelegateFromUserId() != null));
        dto.setTransferred(instances.stream().anyMatch(item -> item.getTransferFromUserId() != null));
        // 3. 最后生成最近动作摘要，供治理列表和详情页快速识别最近一次人工或系统处理。
        dto.setLatestActionSummary(actionLogs.isEmpty() ? null : buildLatestActionSummary(actionLogs.get(0)));
        return dto;
    }

    private void fillExecutionFormMeta(WfExecutionDTO dto, Long taskConfigId) {
        if (taskConfigId == null) {
            return;
        }
        WfTaskConfig taskConfig = taskConfigMapper.selectById(taskConfigId);
        if (taskConfig == null) {
            return;
        }
        dto.setFormType(taskConfig.getFormType());
        dto.setFormPath(taskConfig.getFormPath());
        dto.setTaskFormContent(taskConfig.getFormContent());
    }

    private List<WfApprovalInstanceDTO> loadApprovalInstancesQuietly(Long executionId) {
        try {
            return listApprovalInstances(executionId);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private List<WfApprovalActionLogDTO> loadApprovalActionLogsQuietly(Long executionId) {
        try {
            return listApprovalActionLogs(executionId);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private WfApprovalInstanceDTO toApprovalInstanceDTO(WfTaskApprovalInstance entity) {
        WfApprovalInstanceDTO dto = new WfApprovalInstanceDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private WfApprovalActionLogDTO toApprovalActionLogDTO(WfTaskApprovalActionLog entity) {
        WfApprovalActionLogDTO dto = new WfApprovalActionLogDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private String buildLatestActionSummary(WfApprovalActionLogDTO actionLog) {
        if (actionLog == null) {
            return null;
        }
        String operatorName = StringUtils.hasText(actionLog.getOperatorName()) ? actionLog.getOperatorName() : "系统";
        String actionComment = StringUtils.hasText(actionLog.getActionComment()) ? actionLog.getActionComment() : "无备注";
        return operatorName + " / " + actionComment;
    }

    private void validateExecutionExists(Long executionId) {
        if (executionId == null || executionMapper.selectById(executionId) == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_EXECUTION_NOT_FOUND);
        }
    }

    private Page<WfExecutionDTO> convertToDTOPage(Page<WfTaskExecution> page) {
        Page<WfExecutionDTO> dtoPage = new Page<>();
        dtoPage.setCurrent(page.getCurrent());
        dtoPage.setSize(page.getSize());
        dtoPage.setTotal(page.getTotal());
        dtoPage.setRecords(page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        return dtoPage;
    }

    /**
     * 获取动作处理器工厂。
     * <p>
     * 这里通过 {@link ObjectProvider} 延迟获取工厂实例，避免审批执行服务在 Bean 构造阶段
     * 立即反向拉起处理器链，从而触发循环依赖。
     * </p>
     *
     * @return 动作处理器工厂
     */
    private ApprovalActionHandlerFactory getApprovalActionHandlerFactory() {
        return approvalActionHandlerFactoryProvider.getObject();
    }

    private Long requireCurrentUserId() {
        Long userId = CurrentUserUtils.getUserId();
        if (userId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_USER_INFO_NOT_FOUND);
        }
        return userId;
    }

    private Long requireCurrentTenantId() {
        Long tenantId = CurrentUserUtils.getTenantId();
        if (tenantId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, WorkflowPromptEnum.WF_TENANT_INFO_NOT_FOUND);
        }
        return tenantId;
    }

    private String resolveUsername(Long userId) {
        String username = userInfoService.getUsernameById(userId);
        if (StringUtils.hasText(username)) {
            return username;
        }

        String currentAccount = CurrentUserUtils.getAccount();
        if (StringUtils.hasText(currentAccount)) {
            return currentAccount;
        }
        return "用户" + userId;
    }

    private void populateContext(ApprovalContext context,
                                 WfTaskExecution execution,
                                 WfTaskConfig taskConfig,
                                 Long currentNodeId,
                                 String currentNodeName) {
        context.setExecutionId(execution.getId());
        context.setTaskConfigId(execution.getTaskConfigId());
        context.setTaskCode(execution.getTaskCode());
        context.setTaskName(execution.getTaskName());
        context.setCurrentNodeId(currentNodeId);
        context.setCurrentNodeName(currentNodeName);
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

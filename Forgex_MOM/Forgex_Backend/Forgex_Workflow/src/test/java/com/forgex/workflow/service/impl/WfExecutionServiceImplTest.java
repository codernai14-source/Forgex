package com.forgex.workflow.service.impl;

import com.forgex.common.api.service.UserInfoService;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.UserContext;
import com.forgex.workflow.common.WorkflowConstants;
import com.forgex.workflow.domain.entity.WfTaskApprovalInstance;
import com.forgex.workflow.domain.entity.WfTaskApprovalActionLog;
import com.forgex.workflow.domain.entity.WfMyTask;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.domain.entity.WfTaskExecutionDetail;
import com.forgex.workflow.domain.param.WfExecutionDelegateParam;
import com.forgex.workflow.domain.param.WfExecutionDelegateSaveParam;
import com.forgex.workflow.domain.param.WfExecutionApproveParam;
import com.forgex.workflow.mapper.WfMyTaskMapper;
import com.forgex.workflow.mapper.WfTaskApprovalActionLogMapper;
import com.forgex.workflow.mapper.WfTaskApprovalInstanceMapper;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.mapper.WfTaskExecutionApproverMapper;
import com.forgex.workflow.mapper.WfTaskExecutionDetailMapper;
import com.forgex.workflow.mapper.WfTaskExecutionMapper;
import com.forgex.workflow.mapper.WfTaskNodeConfigMapper;
import com.forgex.workflow.mapper.WfTaskNodeRuleMapper;
import com.forgex.workflow.service.ApprovalNodeActionPolicy;
import com.forgex.workflow.service.IWfEngineService;
import com.forgex.workflow.service.WorkflowNotificationService;
import com.forgex.workflow.service.handler.ApprovalActionHandlerFactory;
import com.forgex.workflow.service.interpreter.ApprovalInterpreterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WfExecutionServiceImplTest {

    @Mock private WfTaskExecutionMapper executionMapper;
    @Mock private WfTaskExecutionDetailMapper executionDetailMapper;
    @Mock private WfTaskExecutionApproverMapper executionApproverMapper;
    @Mock private WfMyTaskMapper myTaskMapper;
    @Mock private WfTaskConfigMapper taskConfigMapper;
    @Mock private WfTaskNodeConfigMapper nodeConfigMapper;
    @Mock private WfTaskNodeRuleMapper nodeRuleMapper;
    @Mock private WfTaskApprovalInstanceMapper approvalInstanceMapper;
    @Mock private WfTaskApprovalActionLogMapper approvalActionLogMapper;
    @Mock private IWfEngineService engineService;
    @Mock private UserInfoService userInfoService;
    @Mock private ApprovalInterpreterRegistry interpreterRegistry;
    @Mock private WorkflowNotificationService workflowNotificationService;
    @Mock private ApprovalNodeActionPolicy approvalNodeActionPolicy;
    @Mock private ObjectProvider<ApprovalActionHandlerFactory> approvalActionHandlerFactoryProvider;

    @InjectMocks
    private WfExecutionServiceImpl service;

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void delegateShouldRejectInactivePendingInstance() {
        UserContext.set(100L);
        WfTaskExecution execution = new WfTaskExecution();
        execution.setId(10L);
        execution.setStatus(WorkflowConstants.ExecutionStatus.PROCESSING);
        when(executionMapper.selectById(10L)).thenReturn(execution);

        WfTaskApprovalInstance instance = new WfTaskApprovalInstance();
        instance.setId(20L);
        instance.setExecutionId(10L);
        instance.setApproverId(100L);
        instance.setStatus(WorkflowConstants.ApprovalInstanceStatus.PENDING);
        instance.setActivated(false);
        when(approvalInstanceMapper.selectById(20L)).thenReturn(instance);

        WfExecutionDelegateParam param = new WfExecutionDelegateParam();
        param.setExecutionId(10L);
        param.setApprovalInstanceId(20L);
        param.setTargetApproverId(200L);

        assertThrows(I18nBusinessException.class, () -> service.delegate(param));
        verify(approvalNodeActionPolicy, never()).requireAllowed(instance.getSourceRuleId(), ApprovalNodeActionPolicy.Action.DELEGATE);
        verify(approvalInstanceMapper, never()).updateById(instance);
    }

    @Test
    void approveShouldRejectInstanceFromAnotherExecution() {
        UserContext.set(100L);
        WfTaskExecution execution = runningExecution(10L, 40L);
        when(executionMapper.selectById(10L)).thenReturn(execution);

        WfMyTask pendingTask = pendingTask(60L, 10L, 30L, 40L, 20L, 100L);
        when(myTaskMapper.selectList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of(pendingTask));
        when(approvalInstanceMapper.selectOne(org.mockito.ArgumentMatchers.any()))
                .thenReturn(pendingInstance(20L, 10L, 30L, 40L, 100L));

        WfTaskExecutionDetail detail = executionDetail(30L, 10L, 40L);
        when(executionDetailMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(detail);

        WfTaskApprovalInstance forgedInstance = pendingInstance(21L, 11L, 30L, 40L, 100L);
        when(approvalInstanceMapper.selectById(21L)).thenReturn(forgedInstance);

        WfExecutionApproveParam param = approveParam(10L, 21L);

        assertThrows(I18nBusinessException.class, () -> service.approve(param));
        verify(executionApproverMapper, never()).insert(
                org.mockito.ArgumentMatchers.any(com.forgex.workflow.domain.entity.WfTaskExecutionApprover.class));
        verify(approvalInstanceMapper, never()).updateById(forgedInstance);
    }

    @Test
    void approveShouldRejectProcessedInstance() {
        UserContext.set(100L);
        WfTaskExecution execution = runningExecution(10L, 40L);
        when(executionMapper.selectById(10L)).thenReturn(execution);

        WfMyTask pendingTask = pendingTask(60L, 10L, 30L, 40L, 20L, 100L);
        when(myTaskMapper.selectList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of(pendingTask));
        when(approvalInstanceMapper.selectOne(org.mockito.ArgumentMatchers.any()))
                .thenReturn(pendingInstance(20L, 10L, 30L, 40L, 100L));

        WfTaskExecutionDetail detail = executionDetail(30L, 10L, 40L);
        when(executionDetailMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(detail);

        WfTaskApprovalInstance processedInstance = pendingInstance(20L, 10L, 30L, 40L, 100L);
        processedInstance.setStatus(WorkflowConstants.ApprovalInstanceStatus.APPROVED);
        processedInstance.setActivated(false);
        when(approvalInstanceMapper.selectById(20L)).thenReturn(processedInstance);

        WfExecutionApproveParam param = approveParam(10L, 20L);

        assertThrows(I18nBusinessException.class, () -> service.approve(param));
        verify(executionApproverMapper, never()).insert(
                org.mockito.ArgumentMatchers.any(com.forgex.workflow.domain.entity.WfTaskExecutionApprover.class));
        verify(approvalInstanceMapper, never()).updateById(processedInstance);
    }

    @Test
    void saveDelegateShouldRejectForgedDelegatorUserId() {
        UserContext.set(100L);
        WfExecutionDelegateSaveParam param = new WfExecutionDelegateSaveParam();
        param.setDelegatorUserId(999L);
        param.setDelegateUserId(200L);

        assertThrows(I18nBusinessException.class, () -> service.handleSaveDelegateAction(param));
        verify(approvalInstanceMapper, never()).selectList(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void saveDelegateShouldRejectAlreadyDelegatedInstance() {
        UserContext.set(100L);
        WfExecutionDelegateSaveParam param = new WfExecutionDelegateSaveParam();
        param.setDelegatorUserId(100L);
        param.setDelegateUserId(200L);

        WfTaskApprovalInstance instance = pendingInstance(20L, 10L, 30L, 40L, 100L);
        instance.setSourceRuleId(50L);
        instance.setDelegateFromUserId(90L);
        when(approvalInstanceMapper.selectList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of(instance));

        assertThrows(I18nBusinessException.class, () -> service.handleSaveDelegateAction(param));
        verify(approvalNodeActionPolicy, never()).resolve(50L);
        verify(approvalInstanceMapper, never()).updateById(instance);
    }

    @Test
    void delegateShouldMoveOnlyTheSelectedPendingInstance() {
        UserContext.set(100L);
        WfTaskExecution execution = new WfTaskExecution();
        execution.setId(10L);
        execution.setTaskName("采购审批");
        execution.setStatus(WorkflowConstants.ExecutionStatus.PROCESSING);
        execution.setTenantId(1L);
        when(executionMapper.selectById(10L)).thenReturn(execution);

        WfTaskApprovalInstance instance = new WfTaskApprovalInstance();
        instance.setId(20L);
        instance.setExecutionId(10L);
        instance.setExecutionDetailId(30L);
        instance.setNodeId(40L);
        instance.setSourceRuleId(50L);
        instance.setApproverId(100L);
        instance.setApproverName("原审批人");
        instance.setStatus(WorkflowConstants.ApprovalInstanceStatus.PENDING);
        instance.setActivated(true);
        when(approvalInstanceMapper.selectById(20L)).thenReturn(instance);

        WfMyTask originalTask = new WfMyTask();
        originalTask.setId(60L);
        originalTask.setApprovalInstanceId(20L);
        originalTask.setStatus(0);
        when(myTaskMapper.selectList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of(originalTask));

        WfTaskExecutionDetail detail = new WfTaskExecutionDetail();
        detail.setId(30L);
        detail.setExecutionId(10L);
        detail.setNodeId(40L);
        detail.setNodeName("部门审批");
        when(executionDetailMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(detail);
        when(userInfoService.getUsernameById(100L)).thenReturn("原审批人");
        when(userInfoService.getUsernameById(200L)).thenReturn("受托人");

        WfExecutionDelegateParam param = new WfExecutionDelegateParam();
        param.setExecutionId(10L);
        param.setApprovalInstanceId(20L);
        param.setTargetApproverId(200L);
        param.setComment("出差委托");

        service.delegate(param);

        verify(approvalNodeActionPolicy).requireAllowed(50L, ApprovalNodeActionPolicy.Action.DELEGATE);
        verify(approvalInstanceMapper).updateById(instance);
        verify(approvalInstanceMapper, never()).insert(org.mockito.ArgumentMatchers.any(WfTaskApprovalInstance.class));
        org.junit.jupiter.api.Assertions.assertEquals(20L, instance.getId());
        org.junit.jupiter.api.Assertions.assertEquals(100L, instance.getDelegateFromUserId());
        org.junit.jupiter.api.Assertions.assertEquals(200L, instance.getApproverId());
        org.junit.jupiter.api.Assertions.assertEquals("受托人", instance.getApproverName());
        org.junit.jupiter.api.Assertions.assertEquals(1, originalTask.getStatus());

        ArgumentCaptor<WfMyTask> newTaskCaptor = ArgumentCaptor.forClass(WfMyTask.class);
        verify(myTaskMapper).insert(newTaskCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(20L, newTaskCaptor.getValue().getApprovalInstanceId());
        org.junit.jupiter.api.Assertions.assertEquals(200L, newTaskCaptor.getValue().getApproverId());
        org.junit.jupiter.api.Assertions.assertEquals(0, newTaskCaptor.getValue().getStatus());
        verify(approvalActionLogMapper).insert(org.mockito.ArgumentMatchers.any(WfTaskApprovalActionLog.class));
    }

    @Test
    void delegateShouldRejectAlreadyDelegatedInstance() {
        UserContext.set(100L);
        WfTaskExecution execution = runningExecution(10L, 40L);
        when(executionMapper.selectById(10L)).thenReturn(execution);

        WfTaskApprovalInstance instance = pendingInstance(20L, 10L, 30L, 40L, 100L);
        instance.setSourceRuleId(50L);
        instance.setDelegateFromUserId(90L);
        when(approvalInstanceMapper.selectById(20L)).thenReturn(instance);

        WfExecutionDelegateParam param = new WfExecutionDelegateParam();
        param.setExecutionId(10L);
        param.setApprovalInstanceId(20L);
        param.setTargetApproverId(200L);

        assertThrows(I18nBusinessException.class, () -> service.delegate(param));
        verify(approvalNodeActionPolicy, never()).requireAllowed(50L, ApprovalNodeActionPolicy.Action.DELEGATE);
        verify(approvalInstanceMapper, never()).updateById(instance);
    }

    @Test
    void delegateShouldCloseEveryOpenTaskForSelectedInstance() {
        UserContext.set(100L);
        WfTaskExecution execution = runningExecution(10L, 40L);
        execution.setTaskName("采购审批");
        execution.setTenantId(1L);
        when(executionMapper.selectById(10L)).thenReturn(execution);

        WfTaskApprovalInstance instance = pendingInstance(20L, 10L, 30L, 40L, 100L);
        instance.setSourceRuleId(50L);
        when(approvalInstanceMapper.selectById(20L)).thenReturn(instance);

        WfMyTask firstTask = pendingTask(60L, 10L, 30L, 40L, 20L, 100L);
        WfMyTask duplicateTask = pendingTask(61L, 10L, 30L, 40L, 20L, 100L);
        when(myTaskMapper.selectList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of(firstTask, duplicateTask));

        when(executionDetailMapper.selectOne(org.mockito.ArgumentMatchers.any()))
                .thenReturn(executionDetail(30L, 10L, 40L));
        when(userInfoService.getUsernameById(100L)).thenReturn("原审批人");
        when(userInfoService.getUsernameById(200L)).thenReturn("受托人");

        WfExecutionDelegateParam param = new WfExecutionDelegateParam();
        param.setExecutionId(10L);
        param.setApprovalInstanceId(20L);
        param.setTargetApproverId(200L);

        service.delegate(param);

        org.junit.jupiter.api.Assertions.assertEquals(1, firstTask.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(1, duplicateTask.getStatus());
        verify(myTaskMapper).updateById(firstTask);
        verify(myTaskMapper).updateById(duplicateTask);
    }

    @Test
    void cancelDelegateShouldMovePendingTaskBackToDelegator() {
        UserContext.set(100L);

        WfTaskApprovalInstance instance = new WfTaskApprovalInstance();
        instance.setId(20L);
        instance.setExecutionId(10L);
        instance.setExecutionDetailId(30L);
        instance.setNodeId(40L);
        instance.setApproverId(200L);
        instance.setDelegateFromUserId(100L);
        instance.setStatus(WorkflowConstants.ApprovalInstanceStatus.PENDING);
        instance.setActivated(true);
        when(approvalInstanceMapper.selectList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of(instance));

        WfMyTask delegatedTask = new WfMyTask();
        delegatedTask.setId(60L);
        delegatedTask.setApprovalInstanceId(20L);
        delegatedTask.setApproverId(200L);
        delegatedTask.setStatus(0);
        when(myTaskMapper.selectList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of(delegatedTask));

        WfTaskExecution execution = new WfTaskExecution();
        execution.setId(10L);
        execution.setTaskName("采购审批");
        execution.setStatus(WorkflowConstants.ExecutionStatus.PROCESSING);
        execution.setTenantId(1L);
        when(executionMapper.selectById(10L)).thenReturn(execution);

        WfTaskExecutionDetail detail = new WfTaskExecutionDetail();
        detail.setId(30L);
        detail.setExecutionId(10L);
        detail.setNodeId(40L);
        detail.setNodeName("部门审批");
        when(executionDetailMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(detail);
        when(userInfoService.getUsernameById(100L)).thenReturn("原审批人");

        service.cancelDelegate(100L);

        org.junit.jupiter.api.Assertions.assertEquals(1, delegatedTask.getStatus());
        verify(myTaskMapper).updateById(delegatedTask);
        org.junit.jupiter.api.Assertions.assertEquals(100L, instance.getApproverId());
        org.junit.jupiter.api.Assertions.assertNull(instance.getDelegateFromUserId());

        ArgumentCaptor<WfMyTask> restoredTaskCaptor = ArgumentCaptor.forClass(WfMyTask.class);
        verify(myTaskMapper).insert(restoredTaskCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(20L, restoredTaskCaptor.getValue().getApprovalInstanceId());
        org.junit.jupiter.api.Assertions.assertEquals(100L, restoredTaskCaptor.getValue().getApproverId());
        org.junit.jupiter.api.Assertions.assertEquals(0, restoredTaskCaptor.getValue().getStatus());
    }

    private WfTaskExecution runningExecution(Long executionId, Long nodeId) {
        WfTaskExecution execution = new WfTaskExecution();
        execution.setId(executionId);
        execution.setCurrentNodeId(nodeId);
        execution.setStatus(WorkflowConstants.ExecutionStatus.PROCESSING);
        return execution;
    }

    private WfTaskApprovalInstance pendingInstance(Long instanceId,
                                                   Long executionId,
                                                   Long executionDetailId,
                                                   Long nodeId,
                                                   Long approverId) {
        WfTaskApprovalInstance instance = new WfTaskApprovalInstance();
        instance.setId(instanceId);
        instance.setExecutionId(executionId);
        instance.setExecutionDetailId(executionDetailId);
        instance.setNodeId(nodeId);
        instance.setApproverId(approverId);
        instance.setStatus(WorkflowConstants.ApprovalInstanceStatus.PENDING);
        instance.setActivated(true);
        return instance;
    }

    private WfMyTask pendingTask(Long taskId,
                                 Long executionId,
                                 Long executionDetailId,
                                 Long nodeId,
                                 Long approvalInstanceId,
                                 Long approverId) {
        WfMyTask task = new WfMyTask();
        task.setId(taskId);
        task.setExecutionId(executionId);
        task.setExecutionDetailId(executionDetailId);
        task.setNodeId(nodeId);
        task.setApprovalInstanceId(approvalInstanceId);
        task.setApproverId(approverId);
        task.setStatus(0);
        return task;
    }

    private WfTaskExecutionDetail executionDetail(Long detailId, Long executionId, Long nodeId) {
        WfTaskExecutionDetail detail = new WfTaskExecutionDetail();
        detail.setId(detailId);
        detail.setExecutionId(executionId);
        detail.setNodeId(nodeId);
        detail.setNodeName("部门审批");
        return detail;
    }

    private WfExecutionApproveParam approveParam(Long executionId, Long instanceId) {
        WfExecutionApproveParam param = new WfExecutionApproveParam();
        param.setExecutionId(executionId);
        param.setApprovalInstanceId(instanceId);
        param.setApproveStatus(1);
        param.setComment("同意");
        return param;
    }
}

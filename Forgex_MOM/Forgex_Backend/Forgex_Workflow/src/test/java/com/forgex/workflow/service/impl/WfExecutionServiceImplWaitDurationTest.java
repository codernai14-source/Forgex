package com.forgex.workflow.service.impl;

import com.forgex.common.api.service.UserInfoService;
import com.forgex.workflow.common.WorkflowConstants;
import com.forgex.workflow.domain.dto.WfApprovalInstanceDTO;
import com.forgex.workflow.domain.dto.WfExecutionDTO;
import com.forgex.workflow.domain.entity.WfMyTask;
import com.forgex.workflow.domain.entity.WfTaskApprovalInstance;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.domain.entity.WfTaskExecutionDetail;
import com.forgex.workflow.mapper.WfMyTaskMapper;
import com.forgex.workflow.mapper.WfTaskCcRecordMapper;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

/**
 * 审批详情等待时长字段填充测试。
 * <p>
 * 覆盖顺序审批未激活实例不参与起点、驳回再进节点取最新明细、
 * 无 pending 不填等待字段，以及 {@code waitingSinceTime} 来自待办而非实例创建时间。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @see WfExecutionServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class WfExecutionServiceImplWaitDurationTest {

    @Mock private WfTaskExecutionMapper executionMapper;
    @Mock private WfTaskExecutionDetailMapper executionDetailMapper;
    @Mock private WfTaskExecutionApproverMapper executionApproverMapper;
    @Mock private WfMyTaskMapper myTaskMapper;
    @Mock private WfTaskConfigMapper taskConfigMapper;
    @Mock private WfTaskNodeConfigMapper nodeConfigMapper;
    @Mock private WfTaskNodeRuleMapper nodeRuleMapper;
    @Mock private WfTaskApprovalInstanceMapper approvalInstanceMapper;
    @Mock private WfTaskApprovalActionLogMapper approvalActionLogMapper;
    @Mock private WfTaskCcRecordMapper ccRecordMapper;
    @Mock private IWfEngineService engineService;
    @Mock private UserInfoService userInfoService;
    @Mock private ApprovalInterpreterRegistry interpreterRegistry;
    @Mock private WorkflowNotificationService workflowNotificationService;
    @Mock private ApprovalNodeActionPolicy approvalNodeActionPolicy;
    @Mock private ObjectProvider<ApprovalActionHandlerFactory> approvalActionHandlerFactoryProvider;

    @InjectMocks
    private WfExecutionServiceImpl service;

    /**
     * 顺序审批第二人未激活、没有待办时，节点等待取当前节点明细时间，
     * 且 {@code waitingSinceTime} 只回填已激活第一人的待办到达时间。
     */
    @Test
    void sequentialInactiveInstanceShouldNotParticipateInWaitStart() {
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 10, 0, 0);
        LocalDateTime nodeArriveTime = LocalDateTime.of(2026, 7, 20, 9, 0, 0);
        LocalDateTime firstTaskTime = LocalDateTime.of(2026, 7, 20, 9, 1, 0);
        LocalDateTime secondInstanceCreateTime = LocalDateTime.of(2026, 7, 20, 9, 0, 0);

        stubExecution(10L, 40L, startTime, WorkflowConstants.ExecutionStatus.PROCESSING);
        stubPolicy();

        WfTaskApprovalInstance first = pendingInstance(20L, 10L, 40L, 100L, true);
        first.setCreateTime(nodeArriveTime);
        WfTaskApprovalInstance second = pendingInstance(21L, 10L, 40L, 200L, false);
        second.setCreateTime(secondInstanceCreateTime);
        when(approvalInstanceMapper.selectList(any())).thenReturn(List.of(first, second));

        WfMyTask firstTask = pendingTask(60L, 10L, 40L, 20L, 100L, firstTaskTime);
        when(myTaskMapper.selectList(any())).thenReturn(List.of(firstTask));

        WfTaskExecutionDetail detail = executionDetail(30L, 10L, 40L, nodeArriveTime);
        when(executionDetailMapper.selectOne(any())).thenReturn(detail);
        when(approvalActionLogMapper.selectList(any())).thenReturn(Collections.emptyList());

        WfExecutionDTO dto = service.getExecutionDetail(10L);

        assertEquals(nodeArriveTime, dto.getCurrentNodeArriveTime());
        assertEquals(nodeArriveTime, dto.getCurrentWaitStartTime());
        assertEquals(firstTaskTime, dto.getCurrentApprovalInstances().get(0).getWaitingSinceTime());
        assertNull(dto.getCurrentApprovalInstances().get(1).getWaitingSinceTime());
    }

    /**
     * 驳回后再进入同一节点时，按明细 id DESC 取最新一条作为本次停留起点。
     */
    @Test
    void rejectedReentryShouldUseLatestExecutionDetail() {
        LocalDateTime firstArrive = LocalDateTime.of(2026, 6, 1, 8, 0, 0);
        LocalDateTime secondArrive = LocalDateTime.of(2026, 8, 10, 11, 0, 0);

        stubExecution(10L, 40L, firstArrive, WorkflowConstants.ExecutionStatus.PROCESSING);
        stubPolicy();

        WfTaskApprovalInstance instance = pendingInstance(20L, 10L, 40L, 100L, true);
        when(approvalInstanceMapper.selectList(any())).thenReturn(List.of(instance));

        WfMyTask task = pendingTask(60L, 10L, 40L, 20L, 100L, secondArrive);
        when(myTaskMapper.selectList(any())).thenReturn(List.of(task));

        WfTaskExecutionDetail latestDetail = executionDetail(32L, 10L, 40L, secondArrive);
        when(executionDetailMapper.selectOne(any())).thenReturn(latestDetail);
        when(approvalActionLogMapper.selectList(any())).thenReturn(Collections.emptyList());

        WfExecutionDTO dto = service.getExecutionDetail(10L);

        assertEquals(secondArrive, dto.getCurrentNodeArriveTime());
        assertEquals(secondArrive, dto.getCurrentWaitStartTime());
        assertEquals(32L, latestDetail.getId());
    }

    /**
     * 已结束流程没有激活待办时，不填充执行单等待字段，实例 waitingSinceTime 也为空。
     */
    @Test
    void finishedExecutionShouldNotFillWaitFields() {
        LocalDateTime startTime = LocalDateTime.of(2026, 5, 1, 9, 0, 0);
        stubExecution(10L, 40L, startTime, WorkflowConstants.ExecutionStatus.COMPLETED);
        stubPolicy();

        WfTaskApprovalInstance approved = pendingInstance(20L, 10L, 40L, 100L, false);
        approved.setStatus(WorkflowConstants.ApprovalInstanceStatus.APPROVED);
        approved.setCreateTime(startTime);
        when(approvalInstanceMapper.selectList(any())).thenReturn(List.of(approved));
        when(myTaskMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(approvalActionLogMapper.selectList(any())).thenReturn(Collections.emptyList());

        WfExecutionDTO dto = service.getExecutionDetail(10L);

        assertNull(dto.getCurrentNodeArriveTime());
        assertNull(dto.getCurrentWaitStartTime());
        assertNull(dto.getCurrentDeadlineTime());
        assertNull(dto.getCurrentApprovalInstances().get(0).getWaitingSinceTime());
    }

    /**
     * {@code waitingSinceTime} 必须来自 {@code wf_my_task.create_time}，而不是实例 createTime。
     */
    @Test
    void waitingSinceTimeShouldComeFromMyTaskInsteadOfInstanceCreateTime() {
        LocalDateTime instanceCreateTime = LocalDateTime.of(2026, 7, 1, 8, 0, 0);
        LocalDateTime myTaskCreateTime = LocalDateTime.of(2026, 7, 15, 14, 30, 0);

        stubExecution(10L, 40L, instanceCreateTime, WorkflowConstants.ExecutionStatus.PROCESSING);
        stubPolicy();

        WfTaskApprovalInstance instance = pendingInstance(20L, 10L, 40L, 100L, true);
        instance.setCreateTime(instanceCreateTime);
        when(approvalInstanceMapper.selectList(any())).thenReturn(List.of(instance));

        WfMyTask task = pendingTask(60L, 10L, 40L, 20L, 100L, myTaskCreateTime);
        when(myTaskMapper.selectList(any())).thenReturn(List.of(task));

        List<WfApprovalInstanceDTO> instances = service.listApprovalInstances(10L);

        assertEquals(1, instances.size());
        assertEquals(myTaskCreateTime, instances.get(0).getWaitingSinceTime());
        assertEquals(instanceCreateTime, instances.get(0).getCreateTime());
    }

    /**
     * 历史待办没有 {@code approvalInstanceId} 时，按 nodeId + approverId 回退匹配。
     */
    @Test
    void waitingSinceTimeShouldFallbackToNodeAndApproverWhenInstanceIdMissing() {
        LocalDateTime myTaskCreateTime = LocalDateTime.of(2026, 8, 1, 10, 0, 0);

        stubExecution(10L, 40L, myTaskCreateTime.minusDays(1), WorkflowConstants.ExecutionStatus.PROCESSING);
        stubPolicy();

        WfTaskApprovalInstance instance = pendingInstance(20L, 10L, 40L, 100L, true);
        when(approvalInstanceMapper.selectList(any())).thenReturn(List.of(instance));

        WfMyTask historicalTask = pendingTask(60L, 10L, 40L, null, 100L, myTaskCreateTime);
        when(myTaskMapper.selectList(any())).thenReturn(List.of(historicalTask));

        List<WfApprovalInstanceDTO> instances = service.listApprovalInstances(10L);

        assertEquals(myTaskCreateTime, instances.get(0).getWaitingSinceTime());
    }

    private void stubExecution(Long executionId, Long nodeId, LocalDateTime startTime, Integer status) {
        WfTaskExecution execution = new WfTaskExecution();
        execution.setId(executionId);
        execution.setCurrentNodeId(nodeId);
        execution.setStartTime(startTime);
        execution.setStatus(status);
        when(executionMapper.selectById(executionId)).thenReturn(execution);
    }

    private void stubPolicy() {
        when(approvalNodeActionPolicy.resolve(nullable(Long.class))).thenReturn(
                new ApprovalNodeActionPolicy.Capabilities(false, false, false));
    }

    private WfTaskApprovalInstance pendingInstance(Long instanceId,
                                                   Long executionId,
                                                   Long nodeId,
                                                   Long approverId,
                                                   boolean activated) {
        WfTaskApprovalInstance instance = new WfTaskApprovalInstance();
        instance.setId(instanceId);
        instance.setExecutionId(executionId);
        instance.setNodeId(nodeId);
        instance.setApproverId(approverId);
        instance.setApproverName("用户" + approverId);
        instance.setStatus(WorkflowConstants.ApprovalInstanceStatus.PENDING);
        instance.setActivated(activated);
        instance.setDeleted(0);
        return instance;
    }

    private WfMyTask pendingTask(Long taskId,
                                 Long executionId,
                                 Long nodeId,
                                 Long approvalInstanceId,
                                 Long approverId,
                                 LocalDateTime createTime) {
        WfMyTask task = new WfMyTask();
        task.setId(taskId);
        task.setExecutionId(executionId);
        task.setNodeId(nodeId);
        task.setApprovalInstanceId(approvalInstanceId);
        task.setApproverId(approverId);
        task.setStatus(0);
        task.setCreateTime(createTime);
        return task;
    }

    private WfTaskExecutionDetail executionDetail(Long detailId,
                                                  Long executionId,
                                                  Long nodeId,
                                                  LocalDateTime createTime) {
        WfTaskExecutionDetail detail = new WfTaskExecutionDetail();
        detail.setId(detailId);
        detail.setExecutionId(executionId);
        detail.setNodeId(nodeId);
        detail.setCreateTime(createTime);
        return detail;
    }
}

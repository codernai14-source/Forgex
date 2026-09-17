package com.forgex.workflow.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.api.service.UserInfoService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.workflow.common.WorkflowConstants;
import com.forgex.workflow.domain.dto.WfExecutionDTO;
import com.forgex.workflow.domain.entity.WfMyTask;
import com.forgex.workflow.domain.entity.WfTaskApprovalActionLog;
import com.forgex.workflow.domain.entity.WfTaskApprovalInstance;
import com.forgex.workflow.domain.entity.WfTaskCcRecord;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.domain.entity.WfTaskNodeConfig;
import com.forgex.workflow.domain.param.WfExecutionQueryParam;
import com.forgex.workflow.mapper.WfMyTaskMapper;
import com.forgex.workflow.mapper.WfTaskApprovalActionLogMapper;
import com.forgex.workflow.mapper.WfTaskApprovalInstanceMapper;
import com.forgex.workflow.mapper.WfTaskCcRecordMapper;
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
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

/**
 * 抄送列表聚合与 COPY 兼容分支测试。
 * <p>
 * 覆盖只看当前用户、按流程去重、未读优先，以及运行时表为空时并入历史 COPY 实例。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @see WfExecutionServiceImpl#pageMyCc
 */
@ExtendWith(MockitoExtension.class)
class WfExecutionServiceImplNodeCcTest {

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
     * 初始化抄送列表相关实体的 Lambda 列缓存。
     */
    @BeforeAll
    static void initLambdaCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, WfTaskCcRecord.class);
        TableInfoHelper.initTableInfo(assistant, WfTaskApprovalInstance.class);
        TableInfoHelper.initTableInfo(assistant, WfTaskNodeConfig.class);
        TableInfoHelper.initTableInfo(assistant, WfTaskExecution.class);
        TableInfoHelper.initTableInfo(assistant, WfMyTask.class);
        TableInfoHelper.initTableInfo(assistant, WfTaskApprovalActionLog.class);
    }

    @AfterEach
    void clearContext() {
        UserContext.clear();
        TenantContext.clear();
    }

    /**
     * 当前用户多条抄送按流程去重，未读优先，不会看到他人记录。
     */
    @Test
    void pageMyCcShouldDedupeByExecutionAndPreferUnread() {
        UserContext.set(100L);
        TenantContext.set(1L);

        WfTaskCcRecord unread = ccRecord(10L, "经理审批", LocalDateTime.of(2026, 9, 1, 10, 0),
                WorkflowConstants.CcReadStatus.UNREAD);
        WfTaskCcRecord unreadDup = ccRecord(10L, "经理审批", LocalDateTime.of(2026, 8, 1, 10, 0),
                WorkflowConstants.CcReadStatus.READ);
        WfTaskCcRecord read = ccRecord(20L, "财务审批", LocalDateTime.of(2026, 9, 10, 10, 0),
                WorkflowConstants.CcReadStatus.READ);
        when(ccRecordMapper.selectList(any())).thenReturn(List.of(unread, unreadDup, read));
        when(approvalInstanceMapper.selectList(any())).thenReturn(Collections.emptyList());

        WfTaskExecution first = execution(10L, "采购单A");
        WfTaskExecution second = execution(20L, "采购单B");
        when(executionMapper.selectList(any())).thenReturn(List.of(first, second));
        when(executionMapper.selectById(10L)).thenReturn(first);
        when(executionMapper.selectById(20L)).thenReturn(second);
        when(approvalActionLogMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(myTaskMapper.selectList(any())).thenReturn(Collections.emptyList());

        WfExecutionQueryParam param = new WfExecutionQueryParam();
        param.setPageNum(1);
        param.setPageSize(10);
        Page<WfExecutionDTO> page = service.pageMyCc(param);

        assertEquals(2, page.getRecords().size());
        assertEquals(10L, page.getRecords().get(0).getId());
        assertTrue(Boolean.TRUE.equals(page.getRecords().get(0).getCcUnread()));
        assertEquals("经理审批", page.getRecords().get(0).getCcNodeName());
        assertEquals(20L, page.getRecords().get(1).getId());
        assertTrue(Boolean.FALSE.equals(page.getRecords().get(1).getCcUnread()));
    }

    /**
     * 运行时表无数据时并入历史 COPY 实例，避免旧列表突然变空。
     */
    @Test
    void pageMyCcShouldFallbackToHistoricalCopy() {
        UserContext.set(100L);
        TenantContext.set(1L);

        when(ccRecordMapper.selectList(any())).thenReturn(Collections.emptyList());

        WfTaskApprovalInstance copyInstance = new WfTaskApprovalInstance();
        copyInstance.setExecutionId(30L);
        copyInstance.setNodeId(40L);
        copyInstance.setApproverId(100L);
        copyInstance.setActivated(true);
        when(approvalInstanceMapper.selectList(any())).thenReturn(List.of(copyInstance));

        WfTaskNodeConfig copyNode = new WfTaskNodeConfig();
        copyNode.setId(40L);
        copyNode.setApproveType(WorkflowConstants.ApproveType.COPY);
        copyNode.setDeleted(0);
        when(nodeConfigMapper.selectById(40L)).thenReturn(copyNode);

        WfTaskExecution execution = execution(30L, "历史抄送单");
        when(executionMapper.selectList(any())).thenReturn(List.of(execution));
        when(executionMapper.selectById(30L)).thenReturn(execution);
        when(approvalActionLogMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(myTaskMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(approvalNodeActionPolicy.resolve(nullable(Long.class)))
                .thenReturn(new ApprovalNodeActionPolicy.Capabilities(false, false, false));

        WfExecutionQueryParam param = new WfExecutionQueryParam();
        param.setPageNum(1);
        param.setPageSize(10);
        Page<WfExecutionDTO> page = service.pageMyCc(param);

        assertEquals(1, page.getRecords().size());
        assertEquals(30L, page.getRecords().get(0).getId());
        assertEquals("历史抄送单", page.getRecords().get(0).getTaskName());
    }

    /**
     * 构造当前用户抄送记录。
     *
     * @param executionId 执行单 ID
     * @param nodeName    抄送节点名
     * @param createTime  抄送时间
     * @param readStatus  已读状态
     * @return 抄送记录
     */
    private WfTaskCcRecord ccRecord(Long executionId, String nodeName, LocalDateTime createTime, Integer readStatus) {
        WfTaskCcRecord record = new WfTaskCcRecord();
        record.setExecutionId(executionId);
        record.setCcUserId(100L);
        record.setNodeName(nodeName);
        record.setCreateTime(createTime);
        record.setReadStatus(readStatus);
        return record;
    }

    /**
     * 构造未删除执行单。
     *
     * @param id       执行单 ID
     * @param taskName 任务名
     * @return 执行单
     */
    private WfTaskExecution execution(Long id, String taskName) {
        WfTaskExecution execution = new WfTaskExecution();
        execution.setId(id);
        execution.setTaskName(taskName);
        execution.setDeleted(0);
        execution.setStatus(WorkflowConstants.ExecutionStatus.PROCESSING);
        return execution;
    }
}

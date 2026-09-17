package com.forgex.workflow.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.forgex.common.api.service.UserInfoService;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.workflow.client.SysUserClient;
import com.forgex.workflow.common.WorkflowConstants;
import com.forgex.workflow.domain.entity.WfTaskApprovalInstance;
import com.forgex.workflow.domain.entity.WfTaskCcRecord;
import com.forgex.workflow.domain.entity.WfTaskConfig;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.domain.entity.WfTaskExecutionDetail;
import com.forgex.workflow.domain.entity.WfTaskNodeApprover;
import com.forgex.workflow.domain.entity.WfTaskNodeCc;
import com.forgex.workflow.domain.entity.WfTaskNodeConfig;
import com.forgex.workflow.enums.WorkflowPromptEnum;
import com.forgex.workflow.mapper.WfMyTaskMapper;
import com.forgex.workflow.mapper.WfTaskApprovalActionLogMapper;
import com.forgex.workflow.mapper.WfTaskApprovalInstanceMapper;
import com.forgex.workflow.mapper.WfTaskCcRecordMapper;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.mapper.WfTaskExecutionApproverMapper;
import com.forgex.workflow.mapper.WfTaskExecutionDetailMapper;
import com.forgex.workflow.mapper.WfTaskExecutionMapper;
import com.forgex.workflow.mapper.WfTaskNodeApproverMapper;
import com.forgex.workflow.mapper.WfTaskNodeCcMapper;
import com.forgex.workflow.mapper.WfTaskNodeConfigMapper;
import com.forgex.workflow.mapper.WfTaskNodeRuleMapper;
import com.forgex.workflow.service.IWfCallbackService;
import com.forgex.workflow.service.WorkflowNotificationService;
import com.forgex.workflow.service.interpreter.ApprovalInterpreterRegistry;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 审批节点激活后独立抄送派发测试。
 * <p>
 * 覆盖开关关闭、解析为空不阻断、与审批人去重、驳回再进再抄送、
 * 以及审批人为空仍失败。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @see WfEngineServiceImpl#dispatchNodeCc
 */
@ExtendWith(MockitoExtension.class)
class WfEngineServiceImplNodeCcTest {

    @Mock private WfTaskConfigMapper taskConfigMapper;
    @Mock private WfTaskNodeConfigMapper nodeConfigMapper;
    @Mock private WfTaskNodeApproverMapper nodeApproverMapper;
    @Mock private WfTaskExecutionMapper executionMapper;
    @Mock private WfTaskExecutionDetailMapper executionDetailMapper;
    @Mock private WfTaskExecutionApproverMapper executionApproverMapper;
    @Mock private WfMyTaskMapper myTaskMapper;
    @Mock private WfTaskApprovalInstanceMapper approvalInstanceMapper;
    @Mock private WfTaskApprovalActionLogMapper approvalActionLogMapper;
    @Mock private WfTaskNodeRuleMapper nodeRuleMapper;
    @Mock private ApprovalInterpreterRegistry interpreterRegistry;
    @Mock private SysUserClient sysUserClient;
    @Mock private UserInfoService userInfoService;
    @Mock private IWfCallbackService callbackService;
    @Mock private WorkflowNotificationService workflowNotificationService;
    @Mock private WfTaskNodeCcMapper nodeCcMapper;
    @Mock private WfTaskCcRecordMapper ccRecordMapper;

    @InjectMocks
    private WfEngineServiceImpl engine;

    /**
     * 初始化抄送实体的 MyBatis-Plus Lambda 列缓存，纯单测没有 Spring 容器。
     */
    @BeforeAll
    static void initLambdaCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, WfTaskCcRecord.class);
        TableInfoHelper.initTableInfo(assistant, WfTaskNodeCc.class);
    }

    /**
     * 开关关闭时不读配置、不落记录。
     */
    @Test
    void dispatchShouldSkipWhenCcDisabled() {
        WfTaskNodeConfig node = approvalNode(40L, 0);
        engine.dispatchNodeCc(execution(), node, detail(30L), new Long[]{200L});
        verify(nodeCcMapper, never()).selectList(any());
        verify(ccRecordMapper, never()).insert(any(WfTaskCcRecord.class));
    }

    /**
     * 抄送解析为空只记日志，不抛异常。
     */
    @Test
    void dispatchShouldNotFailWhenCcUsersEmpty() {
        WfTaskNodeConfig node = approvalNode(40L, 1);
        when(nodeCcMapper.selectList(any())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> engine.dispatchNodeCc(execution(), node, detail(30L), new Long[]{200L}));
        verify(ccRecordMapper, never()).insert(any(WfTaskCcRecord.class));
        verify(workflowNotificationService, never()).notifyCcUsers(any(), any(), any());
    }

    /**
     * 与本节点审批人重叠的用户只进待办，不落抄送。
     */
    @Test
    void dispatchShouldSkipOverlappedApprovers() {
        WfTaskNodeConfig node = approvalNode(40L, 1);
        when(nodeCcMapper.selectList(any())).thenReturn(List.of(ccTarget(1, "[200,300]")));
        when(ccRecordMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(userInfoService.getUsernameMap(any())).thenReturn(Map.of(300L, "抄送人"));
        when(ccRecordMapper.insert(any(WfTaskCcRecord.class))).thenAnswer(invocation -> {
            WfTaskCcRecord record = invocation.getArgument(0);
            record.setId(99L);
            return 1;
        });
        when(workflowNotificationService.notifyCcUsers(any(), any(), any())).thenReturn(true);

        engine.dispatchNodeCc(execution(), node, detail(30L), new Long[]{200L});

        ArgumentCaptor<WfTaskCcRecord> captor = ArgumentCaptor.forClass(WfTaskCcRecord.class);
        verify(ccRecordMapper).insert(captor.capture());
        assertEquals(300L, captor.getValue().getCcUserId());
        verify(workflowNotificationService).notifyCcUsers(any(), any(), any());
    }

    /**
     * 驳回再进产生新明细时，同一用户可以再抄送一次。
     */
    @Test
    void dispatchShouldInsertAgainForNewExecutionDetail() {
        WfTaskNodeConfig node = approvalNode(40L, 1);
        when(nodeCcMapper.selectList(any())).thenReturn(List.of(ccTarget(1, "[300]")));
        when(ccRecordMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(userInfoService.getUsernameMap(any())).thenReturn(Map.of(300L, "抄送人"));
        when(ccRecordMapper.insert(any(WfTaskCcRecord.class))).thenAnswer(invocation -> {
            WfTaskCcRecord record = invocation.getArgument(0);
            record.setId(88L);
            return 1;
        });
        when(workflowNotificationService.notifyCcUsers(any(), any(), any())).thenReturn(false);

        assertDoesNotThrow(() -> engine.dispatchNodeCc(execution(), node, detail(31L), new Long[]{200L}));
        verify(ccRecordMapper).insert(any(WfTaskCcRecord.class));
        verify(ccRecordMapper).updateById(any(WfTaskCcRecord.class));
    }

    /**
     * 节点进入后会落抄送记录；审批人为空仍失败且不派发抄送。
     */
    @Test
    void activateShouldWriteCcAndStillFailWhenApproverEmpty() throws Exception {
        Method method = WfEngineServiceImpl.class.getDeclaredMethod(
                "activateApprovalNode", WfTaskExecution.class, WfTaskNodeConfig.class, WfTaskConfig.class);
        method.setAccessible(true);

        when(executionDetailMapper.insert(any(WfTaskExecutionDetail.class))).thenAnswer(invocation -> {
            ((WfTaskExecutionDetail) invocation.getArgument(0)).setId(30L);
            return 1;
        });
        when(myTaskMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(nodeRuleMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(nodeApproverMapper.selectList(any())).thenReturn(Collections.emptyList());

        InvocationTargetException invoked = assertThrows(InvocationTargetException.class,
                () -> method.invoke(engine, execution(), approvalNode(40L, 1), new WfTaskConfig()));
        I18nBusinessException ex = (I18nBusinessException) invoked.getCause();
        assertEquals(WorkflowPromptEnum.WF_NODE_APPROVER_RESOLVE_EMPTY.getPromptCode(), ex.getMessage());
        verify(ccRecordMapper, never()).insert(any(WfTaskCcRecord.class));
    }

    /**
     * 审批人存在时节点进入会写入抄送快照。
     */
    @Test
    void activateShouldPersistCcRecordAfterPendingNotify() throws Exception {
        Method method = WfEngineServiceImpl.class.getDeclaredMethod(
                "activateApprovalNode", WfTaskExecution.class, WfTaskNodeConfig.class, WfTaskConfig.class);
        method.setAccessible(true);

        when(executionDetailMapper.insert(any(WfTaskExecutionDetail.class))).thenAnswer(invocation -> {
            ((WfTaskExecutionDetail) invocation.getArgument(0)).setId(30L);
            return 1;
        });
        when(myTaskMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(nodeRuleMapper.selectList(any())).thenReturn(Collections.emptyList());

        WfTaskNodeApprover approver = new WfTaskNodeApprover();
        approver.setApproverType(WorkflowConstants.ApproverType.SINGLE);
        approver.setApproverIds("[200]");
        when(nodeApproverMapper.selectList(any())).thenReturn(List.of(approver));
        when(approvalInstanceMapper.insert(any(WfTaskApprovalInstance.class))).thenAnswer(invocation -> {
            ((WfTaskApprovalInstance) invocation.getArgument(0)).setId(20L);
            return 1;
        });

        when(nodeCcMapper.selectList(any())).thenReturn(List.of(ccTarget(1, "[300]")));
        when(ccRecordMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(userInfoService.getUsernameMap(any())).thenReturn(Map.of(300L, "抄送人"));
        when(ccRecordMapper.insert(any(WfTaskCcRecord.class))).thenAnswer(invocation -> {
            ((WfTaskCcRecord) invocation.getArgument(0)).setId(99L);
            return 1;
        });
        when(workflowNotificationService.notifyCcUsers(any(), any(), any())).thenReturn(true);

        method.invoke(engine, execution(), approvalNode(40L, 1), new WfTaskConfig());

        verify(workflowNotificationService).notifyPendingApprovers(any(), any(), any());
        verify(ccRecordMapper, times(1)).insert(any(WfTaskCcRecord.class));
    }

    /**
     * 构造执行单。
     *
     * @return 执行单
     */
    private WfTaskExecution execution() {
        WfTaskExecution execution = new WfTaskExecution();
        execution.setId(10L);
        execution.setTenantId(1L);
        execution.setTaskName("采购审批");
        return execution;
    }

    /**
     * 构造审批节点。
     *
     * @param nodeId    节点 ID
     * @param ccEnabled 抄送开关
     * @return 节点
     */
    private WfTaskNodeConfig approvalNode(Long nodeId, Integer ccEnabled) {
        WfTaskNodeConfig node = new WfTaskNodeConfig();
        node.setId(nodeId);
        node.setNodeName("部门经理");
        node.setNodeType(3);
        node.setCcEnabled(ccEnabled);
        return node;
    }

    /**
     * 构造执行明细。
     *
     * @param detailId 明细 ID
     * @return 明细
     */
    private WfTaskExecutionDetail detail(Long detailId) {
        WfTaskExecutionDetail detail = new WfTaskExecutionDetail();
        detail.setId(detailId);
        detail.setExecutionId(10L);
        detail.setNodeId(40L);
        return detail;
    }

    /**
     * 构造抄送配置。
     *
     * @param type 来源类型
     * @param ids  JSON ID 数组
     * @return 抄送配置
     */
    private WfTaskNodeCc ccTarget(Integer type, String ids) {
        WfTaskNodeCc target = new WfTaskNodeCc();
        target.setId(1L);
        target.setCcType(type);
        target.setCcIds(ids);
        return target;
    }
}

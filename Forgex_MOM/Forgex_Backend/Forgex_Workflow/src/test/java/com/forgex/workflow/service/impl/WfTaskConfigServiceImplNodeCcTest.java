package com.forgex.workflow.service.impl;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.workflow.common.WorkflowConstants;
import com.forgex.workflow.domain.dto.WfNodeApproverDTO;
import com.forgex.workflow.domain.dto.WfTaskNodeEditorDTO;
import com.forgex.workflow.domain.entity.WfTaskNodeCc;
import com.forgex.workflow.enums.WorkflowPromptEnum;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.mapper.WfTaskNodeApproverMapper;
import com.forgex.workflow.mapper.WfTaskNodeCcMapper;
import com.forgex.workflow.mapper.WfTaskNodeConfigMapper;
import com.forgex.workflow.mapper.WfTaskNodeRuleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 审批节点抄送配置保存与校验测试。
 * <p>
 * 覆盖开关关闭可空、开启但未选人失败、混合来源落库。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @see WfTaskConfigServiceImpl#validateNodeCc(WfTaskNodeEditorDTO)
 * @see WfTaskConfigServiceImpl#persistNodeCc(WfTaskNodeEditorDTO, Long, Long)
 */
@ExtendWith(MockitoExtension.class)
class WfTaskConfigServiceImplNodeCcTest {

    @Mock
    private WfTaskConfigMapper taskConfigMapper;

    @Mock
    private WfTaskNodeConfigMapper nodeConfigMapper;

    @Mock
    private WfTaskNodeApproverMapper nodeApproverMapper;

    @Mock
    private WfTaskNodeCcMapper nodeCcMapper;

    @Mock
    private WfTaskNodeRuleMapper nodeRuleMapper;

    @InjectMocks
    private WfTaskConfigServiceImpl service;

    /**
     * 开关关闭时允许不配置抄送对象。
     */
    @Test
    void validateShouldAllowEmptyWhenCcDisabled() {
        WfTaskNodeEditorDTO node = new WfTaskNodeEditorDTO();
        node.setCcEnabled(Boolean.FALSE);

        assertDoesNotThrow(() -> service.validateNodeCc(node));
        service.persistNodeCc(node, 40L, 1L);
        verify(nodeCcMapper, never()).insert(org.mockito.ArgumentMatchers.any(WfTaskNodeCc.class));
    }

    /**
     * 开关开启但未选择有效抄送对象时校验失败。
     */
    @Test
    void validateShouldRequireTargetsWhenCcEnabled() {
        WfTaskNodeEditorDTO node = new WfTaskNodeEditorDTO();
        node.setCcEnabled(Boolean.TRUE);

        I18nBusinessException ex = assertThrows(I18nBusinessException.class, () -> service.validateNodeCc(node));
        assertEquals(WorkflowPromptEnum.WF_NODE_CC_TARGET_REQUIRED.getPromptCode(), ex.getMessage());
        service.persistNodeCc(node, 40L, 1L);
        verify(nodeCcMapper, never()).insert(org.mockito.ArgumentMatchers.any(WfTaskNodeCc.class));
    }

    /**
     * 开启抄送且同时配置用户、部门来源时写入两组记录。
     */
    @Test
    void persistShouldSaveMixedCcSources() {
        WfTaskNodeEditorDTO node = new WfTaskNodeEditorDTO();
        node.setCcEnabled(Boolean.TRUE);
        node.setCcTargets(List.of(
                target(WorkflowConstants.ApproverType.SINGLE, List.of(100L, 101L)),
                target(WorkflowConstants.ApproverType.DEPARTMENT, List.of(200L))
        ));

        assertDoesNotThrow(() -> service.validateNodeCc(node));
        service.persistNodeCc(node, 40L, 1L);

        ArgumentCaptor<WfTaskNodeCc> captor = ArgumentCaptor.forClass(WfTaskNodeCc.class);
        verify(nodeCcMapper, times(2)).insert(captor.capture());
        assertEquals(WorkflowConstants.ApproverType.SINGLE, captor.getAllValues().get(0).getCcType());
        assertEquals("[100,101]", captor.getAllValues().get(0).getCcIds().replace(" ", ""));
        assertEquals(WorkflowConstants.ApproverType.DEPARTMENT, captor.getAllValues().get(1).getCcType());
        assertEquals("[200]", captor.getAllValues().get(1).getCcIds().replace(" ", ""));
    }

    /**
     * 构造一组抄送来源。
     *
     * @param type 来源类型
     * @param ids  来源 ID
     * @return 抄送 DTO
     */
    private WfNodeApproverDTO target(Integer type, List<Long> ids) {
        WfNodeApproverDTO dto = new WfNodeApproverDTO();
        dto.setApproverType(type);
        dto.setApproverIds(ids);
        return dto;
    }
}

package com.forgex.workflow.service;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.workflow.domain.entity.WfTaskNodeRule;
import com.forgex.workflow.mapper.WfTaskNodeRuleMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApprovalNodeActionPolicyTest {

    @Test
    void resolveShouldDisableAllActionsWhenRuleIsMissing() {
        WfTaskNodeRuleMapper ruleMapper = mock(WfTaskNodeRuleMapper.class);
        ApprovalNodeActionPolicy policy = new ApprovalNodeActionPolicy(ruleMapper);

        ApprovalNodeActionPolicy.Capabilities capabilities = policy.resolve(null);

        assertFalse(capabilities.allowAddSign());
        assertFalse(capabilities.allowTransfer());
        assertFalse(capabilities.allowDelegate());
    }

    @Test
    void resolveShouldUseExplicitNodeRuleCapabilities() {
        WfTaskNodeRuleMapper ruleMapper = mock(WfTaskNodeRuleMapper.class);
        WfTaskNodeRule rule = new WfTaskNodeRule();
        rule.setAllowAddSign(true);
        rule.setAllowTransfer(false);
        rule.setAllowDelegate(true);
        when(ruleMapper.selectById(20L)).thenReturn(rule);
        ApprovalNodeActionPolicy policy = new ApprovalNodeActionPolicy(ruleMapper);

        ApprovalNodeActionPolicy.Capabilities capabilities = policy.resolve(20L);

        assertTrue(capabilities.allowAddSign());
        assertFalse(capabilities.allowTransfer());
        assertTrue(capabilities.allowDelegate());
    }

    @Test
    void requireAllowedShouldRejectDisabledAction() {
        WfTaskNodeRuleMapper ruleMapper = mock(WfTaskNodeRuleMapper.class);
        WfTaskNodeRule rule = new WfTaskNodeRule();
        rule.setAllowTransfer(false);
        when(ruleMapper.selectById(30L)).thenReturn(rule);
        ApprovalNodeActionPolicy policy = new ApprovalNodeActionPolicy(ruleMapper);

        assertThrows(I18nBusinessException.class,
                () -> policy.requireAllowed(30L, ApprovalNodeActionPolicy.Action.TRANSFER));
    }
}

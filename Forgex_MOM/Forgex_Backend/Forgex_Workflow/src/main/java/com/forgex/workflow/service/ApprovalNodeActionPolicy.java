package com.forgex.workflow.service;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.workflow.domain.entity.WfTaskNodeRule;
import com.forgex.workflow.enums.WorkflowPromptEnum;
import com.forgex.workflow.mapper.WfTaskNodeRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 审批节点人工动作策略。
 * <p>
 * 统一解析并校验节点规则允许的加签、转交和委托能力，避免查询接口与动作接口
 * 使用不同的默认值或判定方式。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ApprovalNodeActionPolicy {

    private final WfTaskNodeRuleMapper nodeRuleMapper;

    /**
     * 解析指定节点规则的人工动作能力。
     *
     * @param sourceRuleId 审批实例来源规则 ID
     * @return 动作能力；规则缺失或字段为空时均按不允许处理
     */
    public Capabilities resolve(Long sourceRuleId) {
        WfTaskNodeRule rule = sourceRuleId == null ? null : nodeRuleMapper.selectById(sourceRuleId);
        return rule == null
                ? new Capabilities(false, false, false)
                : new Capabilities(
                Boolean.TRUE.equals(rule.getAllowAddSign()),
                Boolean.TRUE.equals(rule.getAllowTransfer()),
                Boolean.TRUE.equals(rule.getAllowDelegate()));
    }

    /**
     * 校验节点是否允许指定人工动作。
     *
     * @param sourceRuleId 审批实例来源规则 ID
     * @param action 待执行动作
     * @throws I18nBusinessException 节点未启用该动作时抛出
     */
    public void requireAllowed(Long sourceRuleId, Action action) {
        Capabilities capabilities = resolve(sourceRuleId);
        boolean allowed = switch (action) {
            case ADD_SIGN -> capabilities.allowAddSign();
            case TRANSFER -> capabilities.allowTransfer();
            case DELEGATE -> capabilities.allowDelegate();
        };
        if (!allowed) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, promptFor(action));
        }
    }

    private WorkflowPromptEnum promptFor(Action action) {
        return switch (action) {
            case ADD_SIGN -> WorkflowPromptEnum.WF_NODE_ADD_SIGN_NOT_ALLOWED;
            case TRANSFER -> WorkflowPromptEnum.WF_NODE_TRANSFER_NOT_ALLOWED;
            case DELEGATE -> WorkflowPromptEnum.WF_NODE_DELEGATE_NOT_ALLOWED;
        };
    }

    /**
     * 节点支持的人工动作。
     */
    public enum Action {
        ADD_SIGN,
        TRANSFER,
        DELEGATE
    }

    /**
     * 节点人工动作能力快照。
     *
     * @param allowAddSign 是否允许加签
     * @param allowTransfer 是否允许转交
     * @param allowDelegate 是否允许委托
     */
    public record Capabilities(boolean allowAddSign, boolean allowTransfer, boolean allowDelegate) {
    }
}

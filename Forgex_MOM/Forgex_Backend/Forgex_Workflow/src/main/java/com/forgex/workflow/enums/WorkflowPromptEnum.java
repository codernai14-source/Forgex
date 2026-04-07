package com.forgex.workflow.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 工作流模块提示枚举
 * <p>
 * 约定：{@link #promptCode} 需与表 {@code fx_i18n_message.prompt_code} 保持一致，
 * 由 {@code module + promptCode} 唯一定位一条国际化文案记录。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>审批发起相关提示消息</li>
 *   <li>审批处理相关提示消息</li>
 *   <li>任务配置相关提示消息</li>
 *   <li>回调处理相关提示消息</li>
 * </ul>
 *
 * @author Forgex
 * @version 1.0.0
 * @see com.forgex.common.service.i18n.I18nMessageService
 * @see com.forgex.common.web.RMessageI18nAdvice
 */
@Getter
public enum WorkflowPromptEnum implements I18nPrompt {
    // ========== 审批发起 ==========
    WF_START_SUCCESS("WF_START_SUCCESS", "发起审批成功"),
    WF_START_FAILED("WF_START_FAILED", "发起审批失败"),
    
    // ========== 审批处理 ==========
    WF_APPROVE_SUCCESS("WF_APPROVE_SUCCESS", "审批通过成功"),
    WF_APPROVE_FAILED("WF_APPROVE_FAILED", "审批通过失败"),
    WF_REJECT_SUCCESS("WF_REJECT_SUCCESS", "审批驳回成功"),
    WF_REJECT_FAILED("WF_REJECT_FAILED", "审批驳回失败"),
    WF_CANCEL_SUCCESS("WF_CANCEL_SUCCESS", "审批取消成功"),
    WF_CANCEL_FAILED("WF_CANCEL_FAILED", "审批取消失败"),
    
    // ========== 任务相关 ==========
    WF_TASK_NOT_FOUND("WF_TASK_NOT_FOUND", "审批任务不存在"),
    WF_ALREADY_PROCESSED("WF_ALREADY_PROCESSED", "该任务已被处理"),
    WF_NO_PERMISSION("WF_NO_PERMISSION", "无权处理该任务"),
    WF_NOT_INITIATOR("WF_NOT_INITIATOR", "只有发起人可以执行此操作"),
    
    // ========== 任务配置 ==========
    WF_TASK_CONFIG_NOT_FOUND("WF_TASK_CONFIG_NOT_FOUND", "审批任务配置不存在"),
    WF_TASK_CONFIG_EXISTS("WF_TASK_CONFIG_EXISTS", "审批任务配置已存在"),
    WF_TASK_CONFIG_CREATE_SUCCESS("WF_TASK_CONFIG_CREATE_SUCCESS", "审批任务配置创建成功"),
    WF_TASK_CONFIG_UPDATE_SUCCESS("WF_TASK_CONFIG_UPDATE_SUCCESS", "审批任务配置更新成功"),
    WF_TASK_CONFIG_DELETE_SUCCESS("WF_TASK_CONFIG_DELETE_SUCCESS", "审批任务配置删除成功"),
    
    // ========== 节点相关 ==========
    WF_NODE_NOT_FOUND("WF_NODE_NOT_FOUND", "审批节点不存在"),
    WF_NODE_CONFIG_INVALID("WF_NODE_CONFIG_INVALID", "审批节点配置无效"),
    
    // ========== 回调处理 ==========
    WF_CALLBACK_SUCCESS("WF_CALLBACK_SUCCESS", "回调处理成功"),
    WF_CALLBACK_FAILED("WF_CALLBACK_FAILED", "回调处理失败");

    private final String promptCode;
    private final String defaultTemplate;

    WorkflowPromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "workflow";
    }
}

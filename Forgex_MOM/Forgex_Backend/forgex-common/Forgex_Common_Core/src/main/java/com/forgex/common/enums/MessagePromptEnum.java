package com.forgex.common.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 消息模板模块提示枚举
 * <p>
 * 约定：{@link #promptCode} 需与表 {@code fx_i18n_message.prompt_code} 保持一致，
 * 由 {@code module + promptCode} 唯一定位一条国际化文案记录。
 * </p>
 *
 * @author Forgex
 * @version 1.0.0
 * @see com.forgex.common.service.i18n.I18nMessageService
 * @see com.forgex.common.web.RMessageI18nAdvice
 */
@Getter
public enum MessagePromptEnum implements I18nPrompt {
    // ========== 消息模板 ==========
    MSG_TEMPLATE_EXISTS("MSG_TEMPLATE_EXISTS", "消息模板已存在"),
    MSG_TEMPLATE_CREATE_SUCCESS("MSG_TEMPLATE_CREATE_SUCCESS", "消息模板创建成功"),
    MSG_TEMPLATE_UPDATE_SUCCESS("MSG_TEMPLATE_UPDATE_SUCCESS", "消息模板更新成功"),
    MSG_TEMPLATE_DELETE_SUCCESS("MSG_TEMPLATE_DELETE_SUCCESS", "消息模板删除成功"),
    MSG_TEMPLATE_NOT_FOUND("MSG_TEMPLATE_NOT_FOUND", "消息模板不存在"),
    
    // ========== 消息发送 ==========
    MSG_SEND_SUCCESS("MSG_SEND_SUCCESS", "消息发送成功"),
    MSG_SEND_FAILED("MSG_SEND_FAILED", "消息发送失败"),
    MSG_SEND_EXCEPTION("MSG_SEND_EXCEPTION", "发送消息异常：{0}"),
    MSG_TEMPLATE_NOT_FOUND_OR_DISABLED("MSG_TEMPLATE_NOT_FOUND_OR_DISABLED", "消息模板不存在或已禁用：{0}"),
    ROCKETMQ_MSG_SEND_FAILED("ROCKETMQ_MSG_SEND_FAILED", "RocketMQ 消息发送失败：{0}"),
    MSG_TEMPLATE_TEST_SUCCESS("MSG_TEMPLATE_TEST_SUCCESS", "消息模板测试成功");

    private final String promptCode;
    private final String defaultTemplate;

    MessagePromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "message";
    }
}

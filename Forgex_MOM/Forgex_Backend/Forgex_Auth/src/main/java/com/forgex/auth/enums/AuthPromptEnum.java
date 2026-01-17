package com.forgex.auth.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 认证模块提示枚举
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
public enum AuthPromptEnum implements I18nPrompt {
    LANG_EMPTY("LANG_EMPTY", "lang不能为空"),
    LANG_SET_FAILED("LANG_SET_FAILED", "设置语言失败"),
    NOT_LOGIN("NOT_LOGIN", "未登录");

    private final String promptCode;
    private final String defaultTemplate;

    AuthPromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "auth";
    }
}

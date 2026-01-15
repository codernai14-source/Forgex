package com.forgex.auth.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

@Getter
public enum AuthPromptEnum implements I18nPrompt {
    LANG_EMPTY("AUTH-LANG-001", "lang不能为空"),
    LANG_SET_FAILED("AUTH-LANG-002", "设置语言失败"),
    NOT_LOGIN("AUTH-LOGIN-401", "未登录");

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


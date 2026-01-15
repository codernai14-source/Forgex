package com.forgex.sys.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

@Getter
public enum SysPromptEnum implements I18nPrompt {
    TABLE_CONFIG_NOT_FOUND("SYS-TABLE-404", "表格配置{0}不存在"),
    TABLE_QUERY_NOT_ALLOWED("SYS-TABLE-403", "表格{0}不允许通用查询");

    private final String promptCode;
    private final String defaultTemplate;

    SysPromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "sys";
    }
}

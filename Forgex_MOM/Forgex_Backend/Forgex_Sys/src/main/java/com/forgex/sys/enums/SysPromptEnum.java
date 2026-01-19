package com.forgex.sys.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 系统模块提示枚举
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
public enum SysPromptEnum implements I18nPrompt {
    TABLE_CONFIG_NOT_FOUND("TABLE_CONFIG_NOT_FOUND", "表格配置{0}不存在"),
    TABLE_QUERY_NOT_ALLOWED("TABLE_QUERY_NOT_ALLOWED", "表格{0}不允许通用查询");

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

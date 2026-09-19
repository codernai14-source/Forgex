package com.forgex.common.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 代码生成模块提示枚举
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
public enum CodeGenPromptEnum implements I18nPrompt {
    // ========== 代码预览与生成 ==========
    CODEGEN_PREVIEW_SUCCESS("CODEGEN_PREVIEW_SUCCESS", "代码预览成功"),
    CODEGEN_GENERATE_SUCCESS("CODEGEN_GENERATE_SUCCESS", "代码生成成功"),
    CODEGEN_GENERATE_FAILED("CODEGEN_GENERATE_FAILED", "代码生成失败"),
    
    // ========== 代码导入 ==========
    CODEGEN_IMPORT_SUCCESS("CODEGEN_IMPORT_SUCCESS", "代码导入成功"),
    CODEGEN_IMPORT_FAILED("CODEGEN_IMPORT_FAILED", "代码导入失败"),
    
    // ========== 配置管理 ==========
    CODEGEN_TABLE_NOT_FOUND("CODEGEN_TABLE_NOT_FOUND", "数据表不存在"),
    CODEGEN_CONFIG_NOT_FOUND("CODEGEN_CONFIG_NOT_FOUND", "生成配置不存在"),
    CODEGEN_CONFIG_SAVE_SUCCESS("CODEGEN_CONFIG_SAVE_SUCCESS", "生成配置保存成功");

    private final String promptCode;
    private final String defaultTemplate;

    CodeGenPromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "codegen";
    }
}

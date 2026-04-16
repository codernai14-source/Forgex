package com.forgex.basic.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 基础模块提示枚举
 * <p>
 * 约定：{@link #promptCode} 需与表 {@code fx_i18n_message.prompt_code} 保持一致，
 * 由 {@code module + promptCode} 唯一定位一条国际化文案记录。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>物料管理相关提示消息</li>
 *   <li>物料扩展相关提示消息</li>
 *   <li>扩展配置相关提示消息</li>
 * </ul>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @see com.forgex.common.service.i18n.I18nMessageService
 * @see com.forgex.common.web.RMessageI18nAdvice
 */
@Getter
public enum BasicPromptEnum implements I18nPrompt {
    // ========== 物料管理 ==========
    MATERIAL_NOT_FOUND("MATERIAL_NOT_FOUND", "物料不存在"),
    MATERIAL_CODE_EXISTS("MATERIAL_CODE_EXISTS", "物料编码已存在"),
    
    // ========== 物料扩展 ==========
    MATERIAL_EXTEND_NOT_FOUND("MATERIAL_EXTEND_NOT_FOUND", "扩展信息不存在"),
    MODULE_CODE_REQUIRED("MODULE_CODE_REQUIRED", "模块编码不能为空"),
    
    // ========== 扩展配置 ==========
    MATERIAL_EXTEND_CONFIG_NOT_FOUND("MATERIAL_EXTEND_CONFIG_NOT_FOUND", "扩展配置不存在"),
    MODULE_CODE_EMPTY("MODULE_CODE_EMPTY", "模块编码不能为空"),
    FIELD_NAME_EXISTS("FIELD_NAME_EXISTS", "模块 [{0}] 下字段 [{1}] 已存在");

    private final String promptCode;
    private final String defaultTemplate;

    BasicPromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "basic";
    }
}

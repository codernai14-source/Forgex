package com.forgex.common.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 仪表盘模块提示枚举
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
public enum DashboardPromptEnum implements I18nPrompt {
    // ========== 数据加载 ==========
    DASHBOARD_DATA_LOAD_SUCCESS("DASHBOARD_DATA_LOAD_SUCCESS", "仪表盘数据加载成功"),
    DASHBOARD_DATA_LOAD_FAILED("DASHBOARD_DATA_LOAD_FAILED", "仪表盘数据加载失败"),
    
    // ========== 组件管理 ==========
    DASHBOARD_WIDGET_ADD_SUCCESS("DASHBOARD_WIDGET_ADD_SUCCESS", "仪表盘组件添加成功"),
    DASHBOARD_WIDGET_REMOVE_SUCCESS("DASHBOARD_WIDGET_REMOVE_SUCCESS", "仪表盘组件移除成功"),
    DASHBOARD_WIDGET_CONFIG_SAVE_SUCCESS("DASHBOARD_WIDGET_CONFIG_SAVE_SUCCESS", "仪表盘组件配置保存成功");

    private final String promptCode;
    private final String defaultTemplate;

    DashboardPromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "dashboard";
    }
}

package com.forgex.report.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 报表模块提示枚举
 * <p>
 * 约定：{@link #promptCode} 需与表 {@code fx_i18n_message.prompt_code} 保持一致，
 * 由 {@code module + promptCode} 唯一定位一条国际化文案记录。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>报表模板相关提示消息</li>
 *   <li>报表分类相关提示消息</li>
 *   <li>报表数据源相关提示消息</li>
 * </ul>
 *
 * @author Forgex
 * @version 1.0.0
 * @see com.forgex.common.service.i18n.I18nMessageService
 * @see com.forgex.common.web.RMessageI18nAdvice
 */
@Getter
public enum ReportPromptEnum implements I18nPrompt {
    // ========== 报表模板 ==========
    REPORT_TEMPLATE_NOT_FOUND("REPORT_TEMPLATE_NOT_FOUND", "报表模板不存在"),
    REPORT_TEMPLATE_CONTENT_EMPTY("REPORT_TEMPLATE_CONTENT_EMPTY", "模板内容为空"),
    REPORT_TEMPLATE_EXPORT_FAILED("REPORT_TEMPLATE_EXPORT_FAILED", "导出模板失败：{0}"),
    REPORT_TEMPLATE_IMPORT_FAILED("REPORT_TEMPLATE_IMPORT_FAILED", "导入模板失败：{0}"),
    REPORT_TEMPLATE_CODE_EXISTS("REPORT_TEMPLATE_CODE_EXISTS", "模板编码已存在"),
    
    // ========== 报表分类 ==========
    REPORT_CATEGORY_NOT_FOUND("REPORT_CATEGORY_NOT_FOUND", "报表分类不存在"),
    REPORT_CATEGORY_CODE_EXISTS("REPORT_CATEGORY_CODE_EXISTS", "分类编码已存在"),
    REPORT_CATEGORY_HAS_CHILDREN("REPORT_CATEGORY_HAS_CHILDREN", "存在子分类，无法删除"),
    
    // ========== 数据源 ==========
    REPORT_DATASOURCE_NOT_FOUND("REPORT_DATASOURCE_NOT_FOUND", "报表数据源不存在"),
    REPORT_DATASOURCE_CODE_EXISTS("REPORT_DATASOURCE_CODE_EXISTS", "数据源编码已存在"),
    REPORT_DATASOURCE_CONNECT_FAILED("REPORT_DATASOURCE_CONNECT_FAILED", "数据源连接失败：{0}");

    private final String promptCode;
    private final String defaultTemplate;

    ReportPromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "report";
    }
}

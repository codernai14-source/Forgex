package com.forgex.common.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * Excel 导入导出模块提示枚举
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
public enum ExcelPromptEnum implements I18nPrompt {
    // ========== Excel 导入 ==========
    EXCEL_IMPORT_SUCCESS("EXCEL_IMPORT_SUCCESS", "Excel 导入成功"),
    EXCEL_IMPORT_FAILED("EXCEL_IMPORT_FAILED", "Excel 导入失败"),
    EXCEL_IMPORT_HANDLER_NOT_CONFIGURED("EXCEL_IMPORT_HANDLER_NOT_CONFIGURED", "导入处理器未配置"),
    EXCEL_IMPORT_HANDLER_NOT_FOUND("EXCEL_IMPORT_HANDLER_NOT_FOUND", "导入处理器不存在：{0}"),
    EXCEL_IMPORT_HANDLER_TYPE_INVALID("EXCEL_IMPORT_HANDLER_TYPE_INVALID", "导入处理器类型不正确：{0}"),
    EXCEL_IMPORT_DATA_EMPTY("EXCEL_IMPORT_DATA_EMPTY", "导入数据不能为空"),
    EXCEL_IMPORT_MODE_INVALID("EXCEL_IMPORT_MODE_INVALID", "导入方式无效"),
    EXCEL_IMPORT_PERMISSION_NOT_CONFIGURED("EXCEL_IMPORT_PERMISSION_NOT_CONFIGURED", "导入权限码未配置"),
    EXCEL_IMPORT_PERMISSION_DENIED("EXCEL_IMPORT_PERMISSION_DENIED", "无导入权限"),
    EXCEL_IMPORT_EXECUTE_SUCCESS("EXCEL_IMPORT_EXECUTE_SUCCESS", "导入完成：共{0}条，新增{1}条，修改{2}条，跳过{3}条，失败{4}条"),
    
    // ========== Excel 导出 ==========
    EXCEL_EXPORT_SUCCESS("EXCEL_EXPORT_SUCCESS", "Excel 导出成功"),
    EXCEL_EXPORT_FAILED("EXCEL_EXPORT_FAILED", "Excel 导出失败"),
    
    // ========== Excel 校验 ==========
    EXCEL_TEMPLATE_NOT_FOUND("EXCEL_TEMPLATE_NOT_FOUND", "Excel 模板不存在"),
    EXCEL_DATA_VALIDATION_FAILED("EXCEL_DATA_VALIDATION_FAILED", "Excel 数据校验失败"),
    EXCEL_IMPORT_TEMPLATE_CONFIG_MISSING("EXCEL_IMPORT_TEMPLATE_CONFIG_MISSING", "导入模板配置不存在或未配置导入字段"),
    EXCEL_IMPORT_TEMPLATE_BUILD_FAILED("EXCEL_IMPORT_TEMPLATE_BUILD_FAILED", "生成导入模板失败: {0}"),
    TEMPLATE_OPTION_PROVIDER_NOT_FOUND("TEMPLATE_OPTION_PROVIDER_NOT_FOUND", "TemplateOptionProvider 不存在：{0}"),
    TEMPLATE_OPTION_LIST_GET_FAILED("TEMPLATE_OPTION_LIST_GET_FAILED", "获取选项列表失败：{0}"),
    
    // ========== Excel 统计 ==========
    EXCEL_ROW_IMPORT_SUCCESS("EXCEL_ROW_IMPORT_SUCCESS", "成功导入{0}行数据"),
    EXCEL_ROW_IMPORT_FAILED("EXCEL_ROW_IMPORT_FAILED", "失败{0}行数据");

    private final String promptCode;
    private final String defaultTemplate;

    ExcelPromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "excel";
    }
}

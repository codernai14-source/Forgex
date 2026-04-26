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
    FIELD_NAME_EXISTS("FIELD_NAME_EXISTS", "模块 [{0}] 下字段 [{1}] 已存在"),

    // ========== 供应商主数据 ==========
    SUPPLIER_CODE_EXISTS("SUPPLIER_CODE_EXISTS", "供应商编码已存在"),
    SUPPLIER_NOT_FOUND("SUPPLIER_NOT_FOUND", "供应商不存在"),
    SUPPLIER_CODE_IMMUTABLE("SUPPLIER_CODE_IMMUTABLE", "供应商编码创建后不可修改"),
    SUPPLIER_LINKED_TENANT_DELETE_FORBIDDEN("SUPPLIER_LINKED_TENANT_DELETE_FORBIDDEN", "已关联租户的供应商不允许删除"),
    SUPPLIER_LINKED_TENANT_NOT_FOUND("SUPPLIER_LINKED_TENANT_NOT_FOUND", "供应商已存在关联租户编号，但系统租户不存在"),
    SUPPLIER_TENANT_CODE_OCCUPIED("SUPPLIER_TENANT_CODE_OCCUPIED", "租户编码已被其它供应商占用"),
    SUPPLIER_TENANT_CREATE_FAILED("SUPPLIER_TENANT_CREATE_FAILED", "创建供应商租户失败"),
    SUPPLIER_ID_REQUIRED("SUPPLIER_ID_REQUIRED", "供应商 ID 不能为空"),
    SUPPLIER_REVIEW_NONE_FORBIDDEN("SUPPLIER_REVIEW_NONE_FORBIDDEN", "无需审查状态不允许发起审查"),
    SUPPLIER_REVIEW_PROCESSING_FORBIDDEN("SUPPLIER_REVIEW_PROCESSING_FORBIDDEN", "审查中的供应商不允许再次发起审查"),
    SUPPLIER_REVIEW_ONLY_PENDING("SUPPLIER_REVIEW_ONLY_PENDING", "仅未审查供应商允许发起审查"),
    SUPPLIER_REVIEW_START_FAILED("SUPPLIER_REVIEW_START_FAILED", "发起供应商资质审查失败"),
    SUPPLIER_REVIEW_CALLBACK_SUPPLIER_ID_MISSING("SUPPLIER_REVIEW_CALLBACK_SUPPLIER_ID_MISSING", "供应商审查回调缺少 supplierId"),
    SUPPLIER_THIRD_PARTY_SYNC_FAILED("SUPPLIER_THIRD_PARTY_SYNC_FAILED", "供应商第三方同步失败"),
    SUPPLIER_IMPORT_FILE_REQUIRED("SUPPLIER_IMPORT_FILE_REQUIRED", "导入文件不能为空"),
    SUPPLIER_PARAM_REQUIRED("SUPPLIER_PARAM_REQUIRED", "供应商参数不能为空"),
    SUPPLIER_CODE_REQUIRED("SUPPLIER_CODE_REQUIRED", "供应商编码不能为空"),
    SUPPLIER_FULL_NAME_REQUIRED("SUPPLIER_FULL_NAME_REQUIRED", "供应商全称不能为空"),
    SUPPLIER_DATA_REQUIRED("SUPPLIER_DATA_REQUIRED", "供应商数据不能为空"),
    SUPPLIER_DEFAULT_TAX_RATE_RANGE("SUPPLIER_DEFAULT_TAX_RATE_RANGE", "默认税率必须在 0 到 100 之间"),
    SUPPLIER_FIELD_VALUE_INVALID("SUPPLIER_FIELD_VALUE_INVALID", "{0}值非法：{1}"),
    SUPPLIER_REVIEW_FORM_SERIALIZE_FAILED("SUPPLIER_REVIEW_FORM_SERIALIZE_FAILED", "供应商审查表单序列化失败"),
    SUPPLIER_REVIEW_CALLBACK_FORM_PARSE_FAILED("SUPPLIER_REVIEW_CALLBACK_FORM_PARSE_FAILED", "供应商审查回调表单解析失败"),
    SUPPLIER_IMPORT_MAIN_SHEET_MISSING("SUPPLIER_IMPORT_MAIN_SHEET_MISSING", "导入文件缺少主表 sheet"),
    SUPPLIER_IMPORT_MAIN_CODE_DUPLICATED("SUPPLIER_IMPORT_MAIN_CODE_DUPLICATED", "主表供应商编码重复：{0}"),
    SUPPLIER_IMPORT_MAIN_SHEET_EMPTY("SUPPLIER_IMPORT_MAIN_SHEET_EMPTY", "导入文件主表不能为空"),
    SUPPLIER_IMPORT_ORPHAN_CODE("SUPPLIER_IMPORT_ORPHAN_CODE", "{0} sheet 第 {1} 行存在孤儿供应商编码：{2}"),
    SUPPLIER_DATE_FORMAT_INVALID("SUPPLIER_DATE_FORMAT_INVALID", "非法日期格式：{0}");

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

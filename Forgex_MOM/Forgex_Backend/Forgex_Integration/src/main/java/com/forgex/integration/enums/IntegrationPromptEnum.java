package com.forgex.integration.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 集成模块提示枚举
 * <p>
 * 约定：{@link #promptCode} 需与表 {@code fx_i18n_message.prompt_code} 保持一致，
 * 由 {@code module + promptCode} 唯一定位一条国际化文案记录。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>API 配置相关提示消息</li>
 *   <li>参数配置相关提示消息</li>
 *   <li>参数映射相关提示消息</li>
 *   <li>API 路由相关提示消息</li>
 *   <li>第三方系统相关提示消息</li>
 *   <li>第三方授权相关提示消息</li>
 * </ul>
 *
 * @author Forgex
 * @version 1.0.0
 * @see com.forgex.common.service.i18n.I18nMessageService
 * @see com.forgex.common.web.RMessageI18nAdvice
 */
@Getter
public enum IntegrationPromptEnum implements I18nPrompt {
    // ========== API 配置 ==========
    API_CONFIG_NOT_FOUND("API_CONFIG_NOT_FOUND", "接口配置不存在"),
    API_CODE_EXISTS("API_CODE_EXISTS", "接口编码已存在：{0}"),
    API_CONFIG_CREATE_FAILED("API_CONFIG_CREATE_FAILED", "创建接口配置失败"),
    API_CONFIG_UPDATE_FAILED("API_CONFIG_UPDATE_FAILED", "更新接口配置失败"),
    API_CONFIG_DELETE_FAILED("API_CONFIG_DELETE_FAILED", "删除接口配置失败"),
    API_CONFIG_ENABLE_FAILED("API_CONFIG_ENABLE_FAILED", "启用接口配置失败"),
    API_CONFIG_DISABLE_FAILED("API_CONFIG_DISABLE_FAILED", "停用接口配置失败"),
    
    // ========== 参数配置 ==========
    PARAM_CONFIG_NOT_FOUND("PARAM_CONFIG_NOT_FOUND", "参数配置不存在"),
    PARAM_FIELD_NAME_REQUIRED("PARAM_FIELD_NAME_REQUIRED", "字段名称不能为空"),
    PARAM_NODE_TYPE_REQUIRED("PARAM_NODE_TYPE_REQUIRED", "节点类型不能为空"),
    PARAM_PARENT_NOT_FOUND("PARAM_PARENT_NOT_FOUND", "父节点不存在"),
    PARAM_CONFIG_CREATE_FAILED("PARAM_CONFIG_CREATE_FAILED", "创建参数配置失败"),
    PARAM_CONFIG_UPDATE_FAILED("PARAM_CONFIG_UPDATE_FAILED", "更新参数配置失败"),
    PARAM_CONFIG_DELETE_FAILED("PARAM_CONFIG_DELETE_FAILED", "删除参数配置失败"),
    PARAM_JSON_PARSE_FAILED("PARAM_JSON_PARSE_FAILED", "解析 JSON 失败：{0}"),
    
    // ========== 参数映射 ==========
    PARAM_MAPPING_NOT_FOUND("PARAM_MAPPING_NOT_FOUND", "参数映射不存在"),
    PARAM_SOURCE_FIELD_REQUIRED("PARAM_SOURCE_FIELD_REQUIRED", "源字段路径不能为空"),
    PARAM_TARGET_FIELD_REQUIRED("PARAM_TARGET_FIELD_REQUIRED", "目标字段路径不能为空"),
    PARAM_DIRECTION_REQUIRED("PARAM_DIRECTION_REQUIRED", "映射方向不能为空"),
    PARAM_MAPPING_EXISTS("PARAM_MAPPING_EXISTS", "映射关系已存在：源字段={0},目标字段={1}"),
    PARAM_MAPPING_CREATE_FAILED("PARAM_MAPPING_CREATE_FAILED", "创建参数映射失败"),
    PARAM_MAPPING_UPDATE_FAILED("PARAM_MAPPING_UPDATE_FAILED", "更新参数映射失败"),
    PARAM_MAPPING_DELETE_FAILED("PARAM_MAPPING_DELETE_FAILED", "删除参数映射失败"),
    
    // ========== API 路由 ==========
    API_HANDLER_NOT_CONFIGURED("API_HANDLER_NOT_CONFIGURED", "未配置接口处理器：{0}"),
    API_HANDLER_NULL("API_HANDLER_NULL", "处理器为空：{0}"),
    API_ROUTE_FAILED("API_ROUTE_FAILED", "路由处理器失败：{0}"),
    API_PARAM_CONVERT_FAILED("API_PARAM_CONVERT_FAILED", "参数转换失败：{0}"),
    
    // ========== 第三方系统 ==========
    THIRD_SYSTEM_NOT_FOUND("THIRD_SYSTEM_NOT_FOUND", "第三方系统不存在"),
    THIRD_SYSTEM_CODE_EXISTS("THIRD_SYSTEM_CODE_EXISTS", "系统编码已存在：{0}"),
    THIRD_SYSTEM_IP_INVALID("THIRD_SYSTEM_IP_INVALID", "第三方系统 IP 仅支持单个地址，不能包含多个 IP"),
    THIRD_SYSTEM_CREATE_FAILED("THIRD_SYSTEM_CREATE_FAILED", "创建第三方系统失败"),
    THIRD_SYSTEM_UPDATE_FAILED("THIRD_SYSTEM_UPDATE_FAILED", "更新第三方系统失败"),
    THIRD_SYSTEM_DELETE_FAILED("THIRD_SYSTEM_DELETE_FAILED", "删除第三方系统失败"),
    
    // ========== 第三方授权 ==========
    THIRD_AUTH_NOT_FOUND("THIRD_AUTH_NOT_FOUND", "第三方授权不存在"),
    THIRD_AUTH_EXISTS("THIRD_AUTH_EXISTS", "该第三方系统已存在授权配置"),
    THIRD_AUTH_CREATE_FAILED("THIRD_AUTH_CREATE_FAILED", "创建第三方授权失败"),
    THIRD_AUTH_UPDATE_FAILED("THIRD_AUTH_UPDATE_FAILED", "更新第三方授权失败"),
    THIRD_AUTH_DELETE_FAILED("THIRD_AUTH_DELETE_FAILED", "删除第三方授权失败"),
    THIRD_AUTH_NOT_CONFIGURED("THIRD_AUTH_NOT_CONFIGURED", "该第三方系统未配置授权信息"),
    THIRD_AUTH_NOT_TOKEN_TYPE("THIRD_AUTH_NOT_TOKEN_TYPE", "该系统的授权方式不是 TOKEN 方式"),
    THIRD_AUTH_WHITELIST_REQUIRED("THIRD_AUTH_WHITELIST_REQUIRED", "白名单授权方式必须配置白名单 IP 列表"),
    THIRD_AUTH_UNSUPPORTED_TYPE("THIRD_AUTH_UNSUPPORTED_TYPE", "不支持的授权方式：{0}"),
    
    // ========== 调用日志 ==========
    CALL_LOG_ID_REQUIRED("CALL_LOG_ID_REQUIRED", "调用记录 ID 不能为空"),
    
    // ========== 通用 ==========
    ID_REQUIRED("ID_REQUIRED", "ID 不能为空"),
    DELETE_IDS_REQUIRED("DELETE_IDS_REQUIRED", "删除 ID 列表不能为空"),
    TENANT_INFO_NOT_FOUND("TENANT_INFO_NOT_FOUND", "无法识别当前租户");

    private final String promptCode;
    private final String defaultTemplate;

    IntegrationPromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "integration";
    }
}

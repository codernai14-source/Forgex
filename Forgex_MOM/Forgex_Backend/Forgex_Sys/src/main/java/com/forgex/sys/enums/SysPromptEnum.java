package com.forgex.sys.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 系统模块提示枚举
 * <p>
 * 约定：{@link #promptCode} 需与表 {@code fx_i18n_message.prompt_code} 保持一致，
 * 由 {@code module + promptCode} 唯一定位一条国际化文案记录。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>用户管理相关提示消息</li>
 *   <li>角色管理相关提示消息</li>
 *   <li>菜单管理相关提示消息</li>
 *   <li>部门管理相关提示消息</li>
 *   <li>职位管理相关提示消息</li>
 *   <li>字典管理相关提示消息</li>
 *   <li>租户管理相关提示消息</li>
 *   <li>模块管理相关提示消息</li>
 *   <li>配置管理相关提示消息</li>
 *   <li>表格配置相关提示消息</li>
 * </ul>
 *
 * @author Forgex
 * @version 1.0.0
 * @see com.forgex.common.service.i18n.I18nMessageService
 * @see com.forgex.common.web.RMessageI18nAdvice
 */
@Getter
public enum SysPromptEnum implements I18nPrompt {
    // ========== 用户管理 ==========
    USER_ACCOUNT_EXISTS("USER_ACCOUNT_EXISTS", "账号已存在"),
    USER_EMAIL_EXISTS("USER_EMAIL_EXISTS", "邮箱已存在"),
    USER_PHONE_EXISTS("USER_PHONE_EXISTS", "手机号已存在"),
    USER_CREATE_SUCCESS("USER_CREATE_SUCCESS", "用户创建成功"),
    USER_UPDATE_SUCCESS("USER_UPDATE_SUCCESS", "用户更新成功"),
    USER_DELETE_SUCCESS("USER_DELETE_SUCCESS", "用户删除成功"),
    USER_NOT_FOUND("USER_NOT_FOUND", "用户不存在"),
    USER_STATUS_UPDATE_SUCCESS("USER_STATUS_UPDATE_SUCCESS", "用户状态更新成功"),
    USER_PASSWORD_RESET_SUCCESS("USER_PASSWORD_RESET_SUCCESS", "用户密码重置成功"),
    USER_PASSWORD_CHANGE_SUCCESS("USER_PASSWORD_CHANGE_SUCCESS", "密码修改成功"),
    USER_PASSWORD_CHANGE_FAILED("USER_PASSWORD_CHANGE_FAILED", "密码修改失败"),
    USER_IMPORT_SUCCESS("USER_IMPORT_SUCCESS", "用户导入成功"),
    USER_EXPORT_SUCCESS("USER_EXPORT_SUCCESS", "用户导出成功"),
    
    // ========== 角色管理 ==========
    ROLE_EXISTS("ROLE_EXISTS", "角色已存在"),
    ROLE_KEY_EXISTS("ROLE_KEY_EXISTS", "角色标识已存在"),
    ROLE_NAME_EXISTS("ROLE_NAME_EXISTS", "角色名称已存在"),
    ROLE_CREATE_SUCCESS("ROLE_CREATE_SUCCESS", "角色创建成功"),
    ROLE_UPDATE_SUCCESS("ROLE_UPDATE_SUCCESS", "角色更新成功"),
    ROLE_DELETE_SUCCESS("ROLE_DELETE_SUCCESS", "角色删除成功"),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "角色不存在"),
    ROLE_HAS_USERS("ROLE_HAS_USERS", "角色下存在关联用户，无法删除"),
    ROLE_ASSIGN_MENU_SUCCESS("ROLE_ASSIGN_MENU_SUCCESS", "角色分配菜单成功"),
    ROLE_ASSIGN_USER_SUCCESS("ROLE_ASSIGN_USER_SUCCESS", "角色分配用户成功"),
    
    // ========== 菜单管理 ==========
    MENU_EXISTS("MENU_EXISTS", "菜单已存在"),
    MENU_PERM_KEY_EXISTS_VALIDATOR("MENU_PERM_KEY_EXISTS", "权限标识已存在"),
    MENU_CREATE_SUCCESS("MENU_CREATE_SUCCESS", "菜单创建成功"),
    MENU_UPDATE_SUCCESS("MENU_UPDATE_SUCCESS", "菜单更新成功"),
    MENU_DELETE_SUCCESS("MENU_DELETE_SUCCESS", "菜单删除成功"),
    MENU_NOT_FOUND("MENU_NOT_FOUND", "菜单不存在"),
    MENU_HAS_CHILDREN("MENU_HAS_CHILDREN", "菜单存在子菜单，无法删除"),
    MENU_HAS_ROLE_ASSOCIATION("MENU_HAS_ROLE_ASSOCIATION", "菜单已被角色关联，无法删除"),
    MENU_ROUTE_GENERATE_SUCCESS("MENU_ROUTE_GENERATE_SUCCESS", "菜单路由生成成功"),
    
    // ========== 部门管理 ==========
    DEPT_EXISTS("DEPT_EXISTS", "部门已存在"),
    DEPT_NAME_EXISTS("DEPT_NAME_EXISTS", "部门名称已存在"),
    DEPT_CREATE_SUCCESS("DEPT_CREATE_SUCCESS", "部门创建成功"),
    DEPT_UPDATE_SUCCESS("DEPT_UPDATE_SUCCESS", "部门更新成功"),
    DEPT_DELETE_SUCCESS("DEPT_DELETE_SUCCESS", "部门删除成功"),
    DEPT_NOT_FOUND("DEPT_NOT_FOUND", "部门不存在"),
    DEPT_HAS_CHILDREN("DEPT_HAS_CHILDREN", "部门存在下级部门，无法删除"),
    DEPT_HAS_USERS("DEPT_HAS_USERS", "部门下存在用户，无法删除"),
    DEPT_PARENT_INVALID("DEPT_PARENT_INVALID", "上级部门不能选择自身或下级部门"),
    
    // ========== 职位管理 ==========
    POSITION_EXISTS("POSITION_EXISTS", "职位已存在"),
    POSITION_CREATE_SUCCESS("POSITION_CREATE_SUCCESS", "职位创建成功"),
    POSITION_UPDATE_SUCCESS("POSITION_UPDATE_SUCCESS", "职位更新成功"),
    POSITION_DELETE_SUCCESS("POSITION_DELETE_SUCCESS", "职位删除成功"),
    POSITION_NOT_FOUND_SERVICE("POSITION_NOT_FOUND", "职位不存在"),
    POSITION_HAS_USERS("POSITION_HAS_USERS", "职位下存在用户，无法删除"),
    
    // ========== 字典管理 ==========
    DICT_TYPE_EXISTS("DICT_TYPE_EXISTS", "字典类型已存在"),
    DICT_TYPE_CREATE_SUCCESS("DICT_TYPE_CREATE_SUCCESS", "字典类型创建成功"),
    DICT_TYPE_UPDATE_SUCCESS("DICT_TYPE_UPDATE_SUCCESS", "字典类型更新成功"),
    DICT_TYPE_DELETE_SUCCESS("DICT_TYPE_DELETE_SUCCESS", "字典类型删除成功"),
    DICT_TYPE_NOT_FOUND("DICT_TYPE_NOT_FOUND", "字典类型不存在"),
    DICT_VALUE_EXISTS("DICT_VALUE_EXISTS", "字典值已存在"),
    DICT_VALUE_CREATE_SUCCESS("DICT_VALUE_CREATE_SUCCESS", "字典值创建成功"),
    DICT_VALUE_UPDATE_SUCCESS("DICT_VALUE_UPDATE_SUCCESS", "字典值更新成功"),
    DICT_VALUE_DELETE_SUCCESS("DICT_VALUE_DELETE_SUCCESS", "字典值删除成功"),
    DICT_VALUE_NOT_FOUND("DICT_VALUE_NOT_FOUND", "字典值不存在"),
    
    // ========== 租户管理 ==========
    TENANT_EXISTS("TENANT_EXISTS", "租户已存在"),
    TENANT_NAME_EXISTS("TENANT_NAME_EXISTS", "租户名称已存在"),
    TENANT_CREATE_SUCCESS("TENANT_CREATE_SUCCESS", "租户创建成功"),
    TENANT_UPDATE_SUCCESS("TENANT_UPDATE_SUCCESS", "租户更新成功"),
    TENANT_DELETE_SUCCESS("TENANT_DELETE_SUCCESS", "租户删除成功"),
    TENANT_NOT_FOUND("TENANT_NOT_FOUND", "租户不存在"),
    TENANT_HAS_USERS("TENANT_HAS_USERS", "租户下存在用户，无法删除"),
    TENANT_STATUS_UPDATE_SUCCESS("TENANT_STATUS_UPDATE_SUCCESS", "租户状态更新成功"),
    
    // ========== 模块管理 ==========
    MODULE_EXISTS("MODULE_EXISTS", "模块已存在"),
    MODULE_CODE_EXISTS_VALIDATOR("MODULE_CODE_EXISTS", "模块编码已存在"),
    MODULE_CREATE_SUCCESS("MODULE_CREATE_SUCCESS", "模块创建成功"),
    MODULE_UPDATE_SUCCESS("MODULE_UPDATE_SUCCESS", "模块更新成功"),
    MODULE_DELETE_SUCCESS("MODULE_DELETE_SUCCESS", "模块删除成功"),
    MODULE_NOT_FOUND("MODULE_NOT_FOUND", "模块不存在"),
    MODULE_HAS_MENUS("MODULE_HAS_MENUS", "模块下存在菜单，无法删除"),
    
    // ========== 配置管理 ==========
    CONFIG_EXISTS("CONFIG_EXISTS", "配置已存在"),
    CONFIG_KEY_EXISTS("CONFIG_KEY_EXISTS", "配置键已存在"),
    CONFIG_CREATE_SUCCESS("CONFIG_CREATE_SUCCESS", "配置创建成功"),
    CONFIG_UPDATE_SUCCESS("CONFIG_UPDATE_SUCCESS", "配置更新成功"),
    CONFIG_DELETE_SUCCESS("CONFIG_DELETE_SUCCESS", "配置删除成功"),
    CONFIG_NOT_FOUND("CONFIG_NOT_FOUND", "配置不存在"),
    
    // ========== 日志管理 ==========
    OPERATION_LOG_QUERY_SUCCESS("OPERATION_LOG_QUERY_SUCCESS", "操作日志查询成功"),
    OPERATION_LOG_EXPORT_SUCCESS("OPERATION_LOG_EXPORT_SUCCESS", "操作日志导出成功"),
    LOGIN_LOG_QUERY_SUCCESS("LOGIN_LOG_QUERY_SUCCESS", "登录日志查询成功"),
    LOGIN_LOG_EXPORT_SUCCESS("LOGIN_LOG_EXPORT_SUCCESS", "登录日志导出成功"),
    
    // ========== 表格配置 ==========
    TABLE_CONFIG_NOT_FOUND("TABLE_CONFIG_NOT_FOUND", "表格配置{0}不存在"),
    TABLE_QUERY_NOT_ALLOWED("TABLE_QUERY_NOT_ALLOWED", "表格{0}不允许通用查询"),
    TABLE_CONFIG_SAVE_SUCCESS("TABLE_CONFIG_SAVE_SUCCESS", "表格配置保存成功"),
    TABLE_CONFIG_RESET_SUCCESS("TABLE_CONFIG_RESET_SUCCESS", "表格配置重置成功"),
    
    // ========== 编码规则管理 ==========
    ENCODE_RULE_EXISTS("ENCODE_RULE_EXISTS", "编码规则已存在"),
    ENCODE_RULE_CODE_EXISTS("ENCODE_RULE_CODE_EXISTS", "规则代码已存在"),
    ENCODE_RULE_CREATE_SUCCESS("ENCODE_RULE_CREATE_SUCCESS", "编码规则创建成功"),
    ENCODE_RULE_UPDATE_SUCCESS("ENCODE_RULE_UPDATE_SUCCESS", "编码规则更新成功"),
    ENCODE_RULE_DELETE_SUCCESS("ENCODE_RULE_DELETE_SUCCESS", "编码规则删除成功"),
    ENCODE_RULE_NOT_FOUND("ENCODE_RULE_NOT_FOUND", "编码规则不存在"),
    ENCODE_RULE_DISABLED("ENCODE_RULE_DISABLED", "编码规则已禁用"),
    ENCODE_RULE_ENABLE_SUCCESS("ENCODE_RULE_ENABLE_SUCCESS", "编码规则启用成功"),
    ENCODE_RULE_DISABLE_SUCCESS("ENCODE_RULE_DISABLE_SUCCESS", "编码规则禁用成功"),
    ENCODE_RULE_GENERATE_SUCCESS("ENCODE_RULE_GENERATE_SUCCESS", "编码生成成功"),
    ENCODE_RULE_GENERATE_FAILED("ENCODE_RULE_GENERATE_FAILED", "编码生成失败"),
    ENCODE_RULE_DETAIL_REQUIRED("ENCODE_RULE_DETAIL_REQUIRED", "编码规则明细不能为空"),
    
    // ========== 用户校验（Validator 层）==========
    USER_ACCOUNT_EMPTY("USER_ACCOUNT_EMPTY", "用户账号不能为空"),
    USER_USERNAME_EMPTY("USER_USERNAME_EMPTY", "用户名不能为空"),
    USER_ACCOUNT_EXISTS_OTHER("USER_ACCOUNT_EXISTS_OTHER", "账号已被其他用户使用"),
    USER_NOT_FOUND_DELETE("USER_NOT_FOUND_DELETE", "用户不存在"),
    USER_ID_INVALID("USER_ID_INVALID", "用户 ID 格式不正确"),
    USER_EMAIL_INVALID("USER_EMAIL_INVALID", "邮箱格式不正确"),
    USER_PHONE_INVALID("USER_PHONE_INVALID", "手机号格式不正确"),
    
    // ========== 角色校验（Validator 层）==========
    ROLE_NAME_EMPTY("ROLE_NAME_EMPTY", "角色名称不能为空"),
    ROLE_CODE_EMPTY("ROLE_CODE_EMPTY", "角色编码不能为空"),
    ROLE_TENANT_ID_EMPTY("ROLE_TENANT_ID_EMPTY", "租户 ID 不能为空"),
    ROLE_CODE_EXISTS_OTHER("ROLE_CODE_EXISTS_OTHER", "角色编码已被其他角色使用"),
    ROLE_NAME_EXISTS_OTHER("ROLE_NAME_EXISTS_OTHER", "角色名称已被其他角色使用"),
    ROLE_KEY_EXISTS_OTHER("ROLE_KEY_EXISTS_OTHER", "角色键已被其他角色使用"),
    ROLE_NOT_FOUND_UPDATE("ROLE_NOT_FOUND_UPDATE", "角色不存在"),
    ROLE_NOT_FOUND_DELETE("ROLE_NOT_FOUND_DELETE", "角色不存在"),
    ROLE_HAS_USERS_DELETE("ROLE_HAS_USERS_DELETE", "该角色下还有用户，无法删除"),
    ROLE_ID_INVALID("ROLE_ID_INVALID", "角色 ID 格式不正确"),
    ROLE_CODE_IMMUTABLE("ROLE_CODE_IMMUTABLE", "角色编码不可修改"),
    
    // ========== 菜单校验（Validator 层）==========
    MENU_MODULE_ID_EMPTY("MENU_MODULE_ID_EMPTY", "模块 ID 不能为空"),
    MENU_NAME_EMPTY("MENU_NAME_EMPTY", "菜单名称不能为空"),
    MENU_TYPE_EMPTY("MENU_TYPE_EMPTY", "菜单类型不能为空"),
    MENU_TYPE_INVALID("MENU_TYPE_INVALID", "菜单类型不正确，只能是：catalog、menu、button"),
    MENU_PATH_EMPTY("MENU_PATH_EMPTY", "菜单路径不能为空"),
    MENU_EXTERNAL_URL_EMPTY("MENU_EXTERNAL_URL_EMPTY", "外联 URL 不能为空"),
    MENU_URL_INVALID("MENU_URL_INVALID", "外联 URL 格式不正确"),
    MENU_PERM_KEY_EMPTY("MENU_PERM_KEY_EMPTY", "按钮权限标识不能为空"),
    MENU_PERM_KEY_INVALID("MENU_PERM_KEY_INVALID", "权限标识格式不正确，必须符合 {module}:{entity}:{action} 格式"),

    MENU_PERM_KEY_EXISTS_OTHER("MENU_PERM_KEY_EXISTS_OTHER", "权限标识已被其他菜单使用"),
    MENU_NOT_FOUND_UPDATE("MENU_NOT_FOUND_UPDATE", "菜单不存在"),
    MENU_NOT_FOUND_DELETE("MENU_NOT_FOUND_DELETE", "菜单不存在"),
    MENU_HAS_CHILDREN_DELETE("MENU_HAS_CHILDREN_DELETE", "该菜单下还有子菜单或按钮，无法删除"),
    MENU_HAS_ROLE_ASSOCIATION_DELETE("MENU_HAS_ROLE_ASSOCIATION_DELETE", "该菜单已被角色授权，无法删除"),
    MENU_ID_INVALID("MENU_ID_INVALID", "菜单 ID 格式不正确"),
    MENU_ACCOUNT_EMPTY("MENU_ACCOUNT_EMPTY", "用户账号不能为空"),
    MENU_TENANT_ID_EMPTY("MENU_TENANT_ID_EMPTY", "租户 ID 不能为空"),
    
    // ========== 模块校验（Validator 层）==========
    MODULE_CODE_EMPTY("MODULE_CODE_EMPTY", "模块编码不能为空"),
    MODULE_NAME_EMPTY("MODULE_NAME_EMPTY", "模块名称不能为空"),

    MODULE_NAME_EXISTS("MODULE_NAME_EXISTS", "模块名称已存在"),
    MODULE_CODE_EXISTS_OTHER("MODULE_CODE_EXISTS_OTHER", "模块编码已被其他模块使用"),
    MODULE_NAME_EXISTS_OTHER("MODULE_NAME_EXISTS_OTHER", "模块名称已被其他模块使用"),
    MODULE_NOT_FOUND_UPDATE("MODULE_NOT_FOUND_UPDATE", "模块不存在"),
    MODULE_NOT_FOUND_DELETE("MODULE_NOT_FOUND_DELETE", "模块不存在"),
    MODULE_HAS_MENUS_DELETE("MODULE_HAS_MENUS_DELETE", "该模块下还有菜单，无法删除"),
    MODULE_HAS_ROLE_ASSOCIATION_DELETE("MODULE_HAS_ROLE_ASSOCIATION_DELETE", "该模块的菜单已被角色授权，无法删除"),
    MODULE_ID_INVALID("MODULE_ID_INVALID", "模块 ID 格式不正确"),
    MODULE_CODE_INVALID("MODULE_CODE_INVALID", "模块编码格式不正确，只能包含字母、数字、下划线，长度 2-50"),
    
    // ========== 角色菜单校验（Validator 层）==========
    ROLE_MENU_ROLE_ID_EMPTY("ROLE_MENU_ROLE_ID_EMPTY", "角色 ID 不能为空"),
    ROLE_MENU_TENANT_ID_EMPTY("ROLE_MENU_TENANT_ID_EMPTY", "租户 ID 不能为空"),
    ROLE_MENU_ROLE_ID_INVALID("ROLE_MENU_ROLE_ID_INVALID", "角色 ID 格式不正确"),
    ROLE_MENU_TENANT_ID_INVALID("ROLE_MENU_TENANT_ID_INVALID", "租户 ID 格式不正确"),
    
    // ========== 用户校验（Service 层）==========
    USER_TENANT_ID_EMPTY("USER_TENANT_ID_EMPTY", "租户 ID 不能为空"),
    USER_NOT_BOUND_TENANT("USER_NOT_BOUND_TENANT", "用户未绑定该租户，无法分配角色"),
    
    // ========== 角色校验（Service 层）==========
    ROLE_INVALID_OR_CROSS_TENANT("ROLE_INVALID_OR_CROSS_TENANT", "存在无效角色或跨租户角色"),
    
    // ========== 职位校验（Service 层）==========

    
    // ========== 编码规则校验（Service 层）==========
    ENCODE_RULE_CODE_EMPTY("ENCODE_RULE_CODE_EMPTY", "规则代码不能为空"),
    ENCODE_RULE_NOT_FOUND_OR_DISABLED("ENCODE_RULE_NOT_FOUND_OR_DISABLED", "编码规则不存在或已禁用：{0}"),
    DELETE_IDS_REQUIRED("DELETE_IDS_REQUIRED", "删除的 ID 列表不能为空"),

    // ========== 代码生成器 ==========
    CODEGEN_PARAM_EMPTY("CODEGEN_PARAM_EMPTY", "代码生成参数不能为空"),
    CODEGEN_DATASOURCE_PARAM_EMPTY("CODEGEN_DATASOURCE_PARAM_EMPTY", "代码生成数据源参数不能为空"),
    CODEGEN_DATASOURCE_NOT_FOUND("CODEGEN_DATASOURCE_NOT_FOUND", "代码生成数据源不存在"),
    CODEGEN_DATASOURCE_CODE_EXISTS("CODEGEN_DATASOURCE_CODE_EXISTS", "代码生成数据源编码已存在"),
    CODEGEN_DATASOURCE_TEST_FAILED("CODEGEN_DATASOURCE_TEST_FAILED", "数据源连接测试失败：{0}"),
    CODEGEN_CONFIG_NOT_FOUND("CODEGEN_CONFIG_NOT_FOUND", "代码生成配置不存在"),
    CODEGEN_TABLE_NOT_FOUND("CODEGEN_TABLE_NOT_FOUND", "数据表不存在：{0}"),
    CODEGEN_MAIN_SUB_RELATION_INVALID("CODEGEN_MAIN_SUB_RELATION_INVALID", "主子表关联字段不匹配"),
    CODEGEN_TREE_FIELD_MISSING("CODEGEN_TREE_FIELD_MISSING", "树形页面缺少必填字段：{0}"),
    CODEGEN_TREE_FILTER_TYPE_INVALID("CODEGEN_TREE_FILTER_TYPE_INVALID", "树主键与右表外键类型不匹配"),
    CODEGEN_TREE_SINGLE_NOT_SELF_REF("CODEGEN_TREE_SINGLE_NOT_SELF_REF", "单表树必须为同一张自关联树表"),
    CODEGEN_PAGE_TYPE_INVALID("CODEGEN_PAGE_TYPE_INVALID", "暂不支持的页面类型：{0}"),
    CODEGEN_PREVIEW_SUCCESS("CODEGEN_PREVIEW_SUCCESS", "代码预览生成成功"),

    // ========== 消息模板校验（Service 层）==========
    MSG_TEMPLATE_CODE_EXISTS("MSG_TEMPLATE_CODE_EXISTS", "模板编号已存在"),
    MSG_TEMPLATE_CODE_EXISTS_OTHER("MSG_TEMPLATE_CODE_EXISTS_OTHER", "模板编号已被其他模板使用"),
    MSG_TEMPLATE_NOT_FOUND("MSG_TEMPLATE_NOT_FOUND", "消息模板不存在"),
    MSG_TEMPLATE_NOT_IN_SCOPE("MSG_TEMPLATE_NOT_IN_SCOPE", "消息模板不存在或不在当前配置范围"),
    MSG_TEMPLATE_PULL_NOT_NEEDED("MSG_TEMPLATE_PULL_NOT_NEEDED", "公共租户无需执行拉取操作"),
    MSG_RECEIVER_IDS_FORMAT_ERROR("MSG_RECEIVER_IDS_FORMAT_ERROR", "接收人 ID 列表格式错误"),
    MSG_TEMPLATE_PARAM_EMPTY("MSG_TEMPLATE_PARAM_EMPTY", "请求参数不能为空"),
    MSG_TEMPLATE_TENANT_NOT_FOUND("MSG_TEMPLATE_TENANT_NOT_FOUND", "无法识别当前租户"),
    
    // ========== 消息发送校验（Service 层）==========
    MSG_NO_PERMISSION("MSG_NO_PERMISSION", "无权向该租户发送消息，请联系管理员配置租户消息白名单"),
    MSG_TEMPLATE_TEST_PARAM_REQUIRED("MSG_TEMPLATE_TEST_PARAM_REQUIRED", "请求参数不能为空");

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

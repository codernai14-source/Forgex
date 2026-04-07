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
    MENU_PERM_KEY_EXISTS("MENU_PERM_KEY_EXISTS", "权限标识已存在"),
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
    POSITION_NOT_FOUND("POSITION_NOT_FOUND", "职位不存在"),
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
    MODULE_CODE_EXISTS("MODULE_CODE_EXISTS", "模块编码已存在"),
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
    TABLE_CONFIG_RESET_SUCCESS("TABLE_CONFIG_RESET_SUCCESS", "表格配置重置成功");

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

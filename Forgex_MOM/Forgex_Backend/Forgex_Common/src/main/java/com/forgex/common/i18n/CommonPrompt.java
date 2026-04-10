package com.forgex.common.i18n;

/**
 * 通用提示枚举
 * <p>
 * 定义系统通用的提示信息，包括成功和失败的提示
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public enum CommonPrompt implements I18nPrompt {

    SAVE_SUCCESS("common", "SAVE_SUCCESS", "保存成功：{0}"),
    CREATE_SUCCESS("common", "CREATE_SUCCESS", "新增成功：{0}"),
    ADD_SUCCESS("common", "ADD_SUCCESS", "添加成功：{0}"),
    UPDATE_SUCCESS("common", "UPDATE_SUCCESS", "修改成功：{0}"),
    DELETE_SUCCESS("common", "DELETE_SUCCESS", "删除成功：{0}"),
    UPLOAD_SUCCESS("common", "UPLOAD_SUCCESS", "上传成功"),
    IMPORT_SUCCESS("common", "IMPORT_SUCCESS", "导入成功"),
    EXPORT_SUCCESS("common", "EXPORT_SUCCESS", "导出成功"),
    SUBMIT_SUCCESS("common", "SUBMIT_SUCCESS", "提交成功"),
    APPROVE_SUCCESS("common", "APPROVE_SUCCESS", "审批成功"),
    REJECT_SUCCESS("common", "REJECT_SUCCESS", "驳回成功"),
    CANCEL_SUCCESS("common", "CANCEL_SUCCESS", "取消成功"),
    ENABLE_SUCCESS("common", "ENABLE_SUCCESS", "启用成功"),
    DISABLE_SUCCESS("common", "DISABLE_SUCCESS", "禁用成功"),
    RESET_SUCCESS("common", "RESET_SUCCESS", "重置成功"),
    COPY_SUCCESS("common", "COPY_SUCCESS", "复制成功"),
    MOVE_SUCCESS("common", "MOVE_SUCCESS", "移动成功"),
    SYNC_SUCCESS("common", "SYNC_SUCCESS", "同步成功"),
    BACKUP_SUCCESS("common", "BACKUP_SUCCESS", "备份成功"),
    RESTORE_SUCCESS("common", "RESTORE_SUCCESS", "恢复成功"),
    PUBLISH_SUCCESS("common", "PUBLISH_SUCCESS", "发布成功"),
    UNPUBLISH_SUCCESS("common", "UNPUBLISH_SUCCESS", "取消发布成功"),
    BIND_SUCCESS("common", "BIND_SUCCESS", "绑定成功"),
    UNBIND_SUCCESS("common", "UNBIND_SUCCESS", "解绑成功"),
    ASSIGN_SUCCESS("common", "ASSIGN_SUCCESS", "分配成功"),
    REVOKE_SUCCESS("common", "REVOKE_SUCCESS", "撤销成功"),
    AUTHORIZE_SUCCESS("common", "AUTHORIZE_SUCCESS", "授权成功"),
    UNAUTHORIZE_SUCCESS("common", "UNAUTHORIZE_SUCCESS", "取消授权成功"),
    INSTALL_SUCCESS("common", "INSTALL_SUCCESS", "安装成功"),
    UNINSTALL_SUCCESS("common", "UNINSTALL_SUCCESS", "卸载成功"),
    START_SUCCESS("common", "START_SUCCESS", "启动成功"),
    STOP_SUCCESS("common", "STOP_SUCCESS", "停止成功"),
    RESTART_SUCCESS("common", "RESTART_SUCCESS", "重启成功"),

    NOT_LOGIN("common", "NOT_LOGIN", "未登录或登录过期"),
    NO_PERMISSION("common", "NO_PERMISSION", "无权限"),
    BAD_REQUEST("common", "BAD_REQUEST", "请求参数错误：{0}"),
    DB_CONNECT_FAILED("common", "DB_CONNECT_FAILED", "数据库连接失败，请检查数据库服务与配置"),
    DATA_ACCESS_ERROR("common", "DATA_ACCESS_ERROR", "数据访问异常"),
    INTERNAL_SERVER_ERROR("common", "INTERNAL_SERVER_ERROR", "服务器内部错误：{0}"),
    PARAM_EMPTY("common", "PARAM_EMPTY", "参数不能为空"),
    ID_EMPTY("common", "ID_EMPTY", "ID不能为空"),
    ID_INVALID("common", "ID_INVALID", "ID格式不正确"),
    NOT_FOUND("common", "NOT_FOUND", "数据不存在"),
    ALREADY_EXISTS("common", "ALREADY_EXISTS", "数据已存在"),
    TENANT_ID_EMPTY("common", "TENANT_ID_EMPTY", "租户ID不能为空"),
    POSITION_NOT_FOUND("common", "POSITION_NOT_FOUND", "职位不存在"),
    DEPARTMENT_NOT_FOUND("common", "DEPARTMENT_NOT_FOUND", "部门不存在"),
    FILE_EMPTY("common", "FILE_EMPTY", "上传文件不能为空"),
    FILE_UPLOAD_FAILED("common", "FILE_UPLOAD_FAILED", "文件上传失败"),
    LANG_CODE_EMPTY("common", "LANG_CODE_EMPTY", "语言代码不能为空"),
    LANG_NAME_EMPTY("common", "LANG_NAME_EMPTY", "语言名称不能为空"),
    LANG_TYPE_NOT_FOUND("common", "LANG_TYPE_NOT_FOUND", "语言类型不存在"),
    DEFAULT_LANG_NOT_FOUND("common", "DEFAULT_LANG_NOT_FOUND", "默认语言类型不存在"),
    ADMIN_NOT_FOUND("common", "ADMIN_NOT_FOUND", "admin用户不存在"),
    ACCOUNT_OR_PASSWORD_EMPTY("common", "ACCOUNT_OR_PASSWORD_EMPTY", "账号或密码不能为空"),
    ACCOUNT_CANNOT_BE_EMPTY("common", "ACCOUNT_CANNOT_BE_EMPTY", "账号不能为空"),
    PASSWORD_CANNOT_BE_EMPTY("common", "PASSWORD_CANNOT_BE_EMPTY", "密码不能为空"),
    OLD_PASSWORD_CANNOT_BE_EMPTY("common", "OLD_PASSWORD_CANNOT_BE_EMPTY", "旧密码不能为空"),
    NEW_PASSWORD_CANNOT_BE_EMPTY("common", "NEW_PASSWORD_CANNOT_BE_EMPTY", "新密码不能为空"),
    PASSWORD_TOO_SHORT("common", "PASSWORD_TOO_SHORT", "密码长度不能少于6位"),
    DEFAULT_PASSWORD_INVALID("common", "DEFAULT_PASSWORD_INVALID", "默认密码不符合密码策略要求"),
    PASSWORD_INCORRECT("common", "PASSWORD_INCORRECT", "密码不正确"),
    ACCOUNT_LOCKED("common", "ACCOUNT_LOCKED", "账号已被锁定，请{0}分钟后再试"),
    VERIFICATION_CODE_CANNOT_BE_EMPTY("common", "VERIFICATION_CODE_CANNOT_BE_EMPTY", "验证码不能为空"),
    VERIFICATION_CODE_INCORRECT("common", "VERIFICATION_CODE_INCORRECT", "验证码不正确"),
    PUBLIC_KEY_NOT_CONFIGURED("common", "PUBLIC_KEY_NOT_CONFIGURED", "未配置公钥"),
    USER_NOT_FOUND("common", "USER_NOT_FOUND", "用户不存在"),
    USER_NOT_BOUND_TO_TENANT("common", "USER_NOT_BOUND_TO_TENANT", "未绑定该租户"),
    USER_ID_CANNOT_BE_EMPTY("common", "USER_ID_CANNOT_BE_EMPTY", "用户ID不能为空"),
    DICT_CODE_CANNOT_BE_EMPTY("common", "DICT_CODE_CANNOT_BE_EMPTY", "字典编码不能为空"),
    NODE_PATH_CANNOT_BE_EMPTY("common", "NODE_PATH_CANNOT_BE_EMPTY", "nodePath不能为空"),
    DICT_ID_CANNOT_BE_EMPTY("common", "DICT_ID_CANNOT_BE_EMPTY", "字典ID不能为空"),
    ACCOUNT_CONFIG_CANNOT_BE_EMPTY("common", "ACCOUNT_CONFIG_CANNOT_BE_EMPTY", "account/config 不能为空"),
    DATA_QUERY_FAILED("common", "DATA_QUERY_FAILED", "数据查询失败：{0}"),
    RESET_FAILED("common", "RESET_FAILED", "重置失败"),
    LOGOUT_FAILED("common", "LOGOUT_FAILED", "退出失败"),
    LANGUAGE_SET_FAILED("common", "LANGUAGE_SET_FAILED", "语言设置失败"),
    INITIALIZING("common", "INITIALIZING", "正在初始化中，请稍后再试"),
    INIT_PASSWORD_INVALID("common", "INIT_PASSWORD_INVALID", "初始化密码不符合安全策略要求"),
    ACCOUNT_EMPTY("common", "ACCOUNT_EMPTY", "account 不能为空"),
    CONFIG_EMPTY("common", "CONFIG_EMPTY", "config 不能为空"),
    MODULE_OFFLINE("common", "MODULE_OFFLINE", "{0}服务未启动"),
    INTERFACE_NOT_FOUND("common", "INTERFACE_NOT_FOUND", "接口不存在"),
    GATEWAY_ERROR("common", "GATEWAY_ERROR", "网关错误"),
    INTERNAL_SERVER_ERROR_MSG("common", "INTERNAL_SERVER_ERROR_MSG", "服务器内部错误：{0}"),
    OPERATION_FAILED("common", "OPERATION_FAILED", "操作失败"),
    OPERATION_SUCCESS("common", "OPERATION_SUCCESS", "操作成功"),

    // 邀请码注册相关
    INVITE_CODE_NOT_FOUND("common", "INVITE_CODE_NOT_FOUND", "邀请码不存在"),
    INVITE_CODE_EXPIRED("common", "INVITE_CODE_EXPIRED", "邀请码已过期"),
    INVITE_CODE_DISABLED("common", "INVITE_CODE_DISABLED", "邀请码已停用"),
    INVITE_CODE_USED_UP("common", "INVITE_CODE_USED_UP", "邀请码已用尽"),
    INVITE_CODE_INVALID("common", "INVITE_CODE_INVALID", "邀请码无效"),
    REGISTER_SUCCESS("common", "REGISTER_SUCCESS", "注册成功"),
    ACCOUNT_ALREADY_EXISTS("common", "ACCOUNT_ALREADY_EXISTS", "账号已存在"),
    INVITE_CODE_CANNOT_BE_EMPTY("common", "INVITE_CODE_CANNOT_BE_EMPTY", "邀请码不能为空");

    private final String module;
    private final String promptCode;
    private final String defaultTemplate;

    CommonPrompt(String module, String promptCode, String defaultTemplate) {
        this.module = module;
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return module;
    }

    @Override
    public String getPromptCode() {
        return promptCode;
    }

    @Override
    public String getDefaultTemplate() {
        return defaultTemplate;
    }
}

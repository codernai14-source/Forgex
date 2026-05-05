package com.forgex.auth.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 认证模块提示枚举
 * <p>
 * 约定：{@link #promptCode} 需与表 {@code fx_i18n_message.prompt_code} 保持一致，
 * 由 {@code module + promptCode} 唯一定位一条国际化文案记录。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>登录认证相关提示消息</li>
 *   <li>Token 管理相关提示消息</li>
 *   <li>权限校验相关提示消息</li>
 *   <li>第三方登录相关提示消息</li>
 *   <li>语言设置相关提示消息</li>
 * </ul>
 *
 * @author Forgex
 * @version 1.0.0
 * @see com.forgex.common.service.i18n.I18nMessageService
 * @see com.forgex.common.web.RMessageI18nAdvice
 */
@Getter
public enum AuthPromptEnum implements I18nPrompt {
    // ========== 语言设置 ==========
    LANG_EMPTY("LANG_EMPTY", "lang不能为空"),
    LANG_SET_FAILED("LANG_SET_FAILED", "设置语言失败"),

    // ========== 登录认证 ==========
    NOT_LOGIN("NOT_LOGIN", "未登录"),
    LOGIN_SUCCESS("LOGIN_SUCCESS", "登录成功"),
    LOGOUT_SUCCESS("LOGOUT_SUCCESS", "登出成功"),
    TENANT_NOT_SELECTED("TENANT_NOT_SELECTED", "未选择租户"),
    TENANT_CHOICE_SUCCESS("TENANT_CHOICE_SUCCESS", "租户选择成功"),
    CAPTCHA_REQUIRED("CAPTCHA_REQUIRED", "验证码为必填项"),
    CAPTCHA_INVALID("CAPTCHA_INVALID", "验证码无效"),
    ACCOUNT_DISABLED("ACCOUNT_DISABLED", "账号已被禁用"),

    // ========== Token 管理 ==========
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token 已过期"),
    TOKEN_INVALID("TOKEN_INVALID", "Token 无效"),

    // ========== 权限校验 ==========
    PERMISSION_DENIED("PERMISSION_DENIED", "权限不足"),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "角色不存在"),

    // ========== 第三方登录 ==========
    SOCIAL_LOGIN_FAILED("SOCIAL_LOGIN_FAILED", "第三方登录失败"),
    SOCIAL_ACCOUNT_BOUND("SOCIAL_ACCOUNT_BOUND", "第三方账号已绑定"),

    // ========== 策略选择 ==========
    CAPTCHA_MODE_NOT_SUPPORTED("CAPTCHA_MODE_NOT_SUPPORTED", "暂不支持的验证码模式: {0}"),
    LOGIN_METHOD_NOT_SUPPORTED("LOGIN_METHOD_NOT_SUPPORTED", "暂不支持的登录方式: terminal={0}, type={1}"),
    TENANT_TERMINAL_NOT_SUPPORTED("TENANT_TERMINAL_NOT_SUPPORTED", "暂不支持的租户选择终端: {0}");

    private final String promptCode;
    private final String defaultTemplate;

    AuthPromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    /**
     * 获取模块。
     *
     * @return 字符串结果
     */
    @Override
    public String getModule() {
        return "auth";
    }
}

package com.forgex.common.security;

import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * 登录失败原因展示文案转换工具。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
public final class LoginFailureReasonResolver {

    private static final Map<String, String> REASON_TEXT_MAP = Map.ofEntries(
        Map.entry("account or password is empty", "账号或密码不能为空"),
        Map.entry("account is locked", "账号已被锁定"),
        Map.entry("user not found", "用户不存在"),
        Map.entry("password incorrect", "密码不正确"),
        Map.entry("captcha missing", "验证码不能为空"),
        Map.entry("captcha incorrect", "验证码不正确"),
        Map.entry("verification code cannot be empty", "验证码不能为空"),
        Map.entry("verification code incorrect", "验证码不正确"),
        Map.entry("image captcha missing", "图片验证码不能为空"),
        Map.entry("image captcha incorrect", "图片验证码错误"),
        Map.entry("slider captcha missing", "滑块验证码不能为空"),
        Map.entry("slider captcha incorrect", "滑块验证码错误")
    );

    private LoginFailureReasonResolver() {
    }

    /**
     * 将历史英文失败原因转换为中文展示文案。
     *
     * @param reason 原始失败原因
     * @return 中文失败原因，无法识别时返回原值
     */
    public static String resolve(String reason) {
        if (!StringUtils.hasText(reason)) {
            return reason;
        }
        String normalized = reason.trim().toLowerCase(Locale.ROOT);
        return REASON_TEXT_MAP.getOrDefault(normalized, reason);
    }
}

package com.forgex.common.security.password;

import com.forgex.common.domain.config.PasswordPolicyConfig;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

/**
 * 统一密码策略校验器。
 * <p>
 * 密码校验必须在进入加密 Provider 前完成，避免不同服务采用不一致的强度规则。
 * 系统配置页的口令过期天数和禁止重复次数也在此补齐默认值。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see PasswordPolicyConfig
 */
public final class PasswordPolicyValidator {

    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "123456", "12345678", "password", "qwerty", "admin", "aa123456", "admin123!"
    );

    private PasswordPolicyValidator() {
    }

    /**
     * 判断密码是否满足策略，并拒绝常见弱口令及与账号相似的口令。
     *
     * @param password 明文密码
     * @param account 账号，可为空
     * @param policy 密码策略
     * @return true 表示通过
     */
    public static boolean isValid(String password, String account, PasswordPolicyConfig policy) {
        if (!StringUtils.hasText(password)) {
            return false;
        }
        String normalized = password.trim();
        if (COMMON_PASSWORDS.contains(normalized.toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (StringUtils.hasText(account)) {
            String normalizedAccount = account.trim().toLowerCase(Locale.ROOT);
            String normalizedPassword = normalized.toLowerCase(Locale.ROOT);
            if (normalizedAccount.length() >= 3
                    && (normalizedPassword.contains(normalizedAccount)
                    || normalizedAccount.contains(normalizedPassword))) {
                return false;
            }
        }
        int minLength = policy == null || policy.getMinLength() == null ? 8 : policy.getMinLength();
        if (normalized.length() < Math.max(8, minLength)) {
            return false;
        }
        if (policy != null && Boolean.TRUE.equals(policy.getRequireNumbers()) && !normalized.matches(".*\\d.*")) {
            return false;
        }
        if (policy != null && Boolean.TRUE.equals(policy.getRequireUppercase()) && !normalized.matches(".*[A-Z].*")) {
            return false;
        }
        if (policy != null && Boolean.TRUE.equals(policy.getRequireLowercase()) && !normalized.matches(".*[a-z].*")) {
            return false;
        }
        return policy == null || !Boolean.TRUE.equals(policy.getRequireSymbols())
                || normalized.matches(".*[^A-Za-z0-9].*");
    }

    /**
     * 要求配置显式提供默认密码，禁止服务自行回退到固定口令。
     *
     * @param policy 密码策略
     * @return 配置的默认密码
     * @throws IllegalStateException 未配置默认密码时抛出
     */
    public static String requireConfiguredDefaultPassword(PasswordPolicyConfig policy) {
        if (policy == null || !StringUtils.hasText(policy.getDefaultPassword())) {
            throw new IllegalStateException("security.password.policy.defaultPassword must be configured explicitly");
        }
        return policy.getDefaultPassword();
    }

    /**
     * 补齐口令过期天数、历史次数和首次强制改密，并限制取值范围。
     * <p>
     * 系统配置读取/保存时调用，避免历史 JSON 缺字段时页面空白，也避免保存时把等保字段写成 {@code null}。
     * </p>
     *
     * @param policy 原始策略，可为 {@code null}
     * @return 规范化后的策略，不会返回 {@code null}
     * @see PasswordPolicyConfig#getMaxAgeDays()
     * @see PasswordPolicyConfig#getHistoryCount()
     */
    public static PasswordPolicyConfig normalizeLifecycle(PasswordPolicyConfig policy) {
        PasswordPolicyConfig normalized = policy == null ? new PasswordPolicyConfig() : policy;
        if (normalized.getExpireEnabled() == null) {
            normalized.setExpireEnabled(Boolean.FALSE);
        }
        Integer legacyMaxAge = normalized.getMaxAgeDays();
        normalized.setExpireDays(clamp(normalized.getExpireDays(), 1, 3650,
                legacyMaxAge != null && legacyMaxAge > 0 ? legacyMaxAge : 90));
        normalized.setExpireWarnDays(clamp(normalized.getExpireWarnDays(), 0, 3650, 7));
        normalized.setMaxAgeDays(clamp(normalized.getMaxAgeDays(), 0, 3650, 90));
        normalized.setHistoryCount(clamp(normalized.getHistoryCount(), 0, 24, 5));
        if (normalized.getForceChangeOnFirstLogin() == null) {
            normalized.setForceChangeOnFirstLogin(Boolean.TRUE);
        }
        if (normalized.getMinStrengthLevel() == null) {
            normalized.setMinStrengthLevel(4);
        }
        return normalized;
    }

    /**
     * 将可空整数限制在闭区间内，空值回落到默认值。
     *
     * @param value        原始值
     * @param min          最小值（含）
     * @param max          最大值（含）
     * @param defaultValue 空值时的默认值
     * @return 规范化后的整数
     */
    private static int clamp(Integer value, int min, int max, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Math.min(max, Math.max(min, value));
    }
}

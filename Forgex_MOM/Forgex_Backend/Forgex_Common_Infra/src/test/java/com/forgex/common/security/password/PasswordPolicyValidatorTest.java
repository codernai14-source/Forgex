package com.forgex.common.security.password;

import com.forgex.common.domain.config.PasswordPolicyConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 统一密码策略校验器测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see PasswordPolicyValidator
 */
class PasswordPolicyValidatorTest {

    private PasswordPolicyConfig policy() {
        PasswordPolicyConfig policy = new PasswordPolicyConfig();
        policy.setMinLength(8);
        policy.setRequireNumbers(true);
        policy.setRequireUppercase(true);
        policy.setRequireLowercase(true);
        policy.setRequireSymbols(true);
        return policy;
    }

    @Test
    void acceptsPasswordMeetingAllConfiguredRules() {
        assertTrue(PasswordPolicyValidator.isValid("Asecure9!", "alice", policy()));
    }

    @Test
    void rejectsCommonWeakPasswordAndAccountSimilarPassword() {
        assertFalse(PasswordPolicyValidator.isValid("Aa123456", "alice", policy()));
        assertFalse(PasswordPolicyValidator.isValid("Alice123!", "alice", policy()));
    }

    @Test
    void rejectsMissingCharacterClasses() {
        assertFalse(PasswordPolicyValidator.isValid("abcdefghi", "alice", policy()));
    }

    /**
     * 缺字段时补齐等保默认过期天数和禁止重复次数。
     */
    @Test
    void fillsLifecycleDefaultsWhenMissing() {
        PasswordPolicyConfig normalized = PasswordPolicyValidator.normalizeLifecycle(null);
        assertEquals(90, normalized.getMaxAgeDays());
        assertEquals(5, normalized.getHistoryCount());
        assertTrue(normalized.getForceChangeOnFirstLogin());
    }

    /**
     * 超范围取值应被限制在允许区间内。
     */
    @Test
    void clampsLifecycleFieldsToAllowedRange() {
        PasswordPolicyConfig policy = new PasswordPolicyConfig();
        policy.setMaxAgeDays(99999);
        policy.setHistoryCount(-3);
        PasswordPolicyConfig normalized = PasswordPolicyValidator.normalizeLifecycle(policy);
        assertEquals(3650, normalized.getMaxAgeDays());
        assertEquals(0, normalized.getHistoryCount());
    }
}

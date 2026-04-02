package com.forgex.sys.domain.config;

import com.forgex.common.domain.config.CryptoTransportConfig;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import lombok.Data;

/**
 * Security configuration aggregate object.
 */
@Data
public class SecurityConfig {

    private CaptchaConfig captcha = CaptchaConfig.defaults();
    private PasswordPolicyConfig passwordPolicy = defaultPasswordPolicy();
    private CryptoTransportConfig cryptoTransport = defaultCryptoTransport();

    public static SecurityConfig defaults() {
        return new SecurityConfig();
    }

    private static PasswordPolicyConfig defaultPasswordPolicy() {
        PasswordPolicyConfig config = new PasswordPolicyConfig();
        config.setStore("bcrypt");
        config.setMinLength(8);
        config.setRequireNumbers(true);
        config.setRequireUppercase(false);
        config.setRequireLowercase(false);
        config.setRequireSymbols(false);
        return config;
    }

    private static CryptoTransportConfig defaultCryptoTransport() {
        CryptoTransportConfig config = new CryptoTransportConfig();
        config.setAlgorithm("SM2");
        config.setCipher("BCD");
        config.setPublicKey("");
        config.setPrivateKey("");
        return config;
    }
}

package com.forgex.common.domain.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 验证码配置。
 * <p>
 * 由系统配置库读取，控制登录流程的验证码模式与滑块参数。
 * 字段：
 * - {@code mode} 验证码模式：image/slider/none；
 * - {@code slider} 滑块参数配置。
 * <p>
 * 使用：通过配置服务读取 {@code login.captcha} 或其它键，若为空可用 {@link #defaults()} 获取默认配置。
 */
@Setter
@Getter
public class CaptchaConfig {
    /** 验证码模式：image/slider/none */
    private String mode;
    /** 滑块参数配置 */
    private SliderConfig slider;

    /**
     * 默认配置生成。
     * @return 默认验证码配置（模式为 image，滑块禁用，令牌 120s）
     */
    public static CaptchaConfig defaults() {
        CaptchaConfig cfg = new CaptchaConfig();
        cfg.setMode("image");
        SliderConfig s = new SliderConfig();
        s.setKeyPrefix("captcha:slider");
        s.setSecondaryKeyPrefix("captcha:secondary");
        s.setSecondaryEnabled(false);
        s.setTokenExpireSeconds(120);
        cfg.setSlider(s);
        return cfg;
    }

    /**
     * 滑块验证码参数配置。
     * 字段：
     * - {@code keyPrefix} 主令牌 Redis 键前缀；
     * - {@code secondaryKeyPrefix} 次令牌键前缀；
     * - {@code secondaryEnabled} 是否开启二次校验；
     * - {@code tokenExpireSeconds} 令牌过期秒数。
     */
    @Setter
    @Getter
    public static class SliderConfig {
        /** 主令牌键前缀 */
        private String keyPrefix;
        /** 次令牌键前缀 */
        private String secondaryKeyPrefix;
        /** 是否开启二次校验 */
        private boolean secondaryEnabled;
        /** 令牌过期秒数 */
        private int tokenExpireSeconds;

    }
}


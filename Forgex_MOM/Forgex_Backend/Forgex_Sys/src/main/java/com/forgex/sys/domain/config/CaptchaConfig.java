package com.forgex.sys.domain.config;

import lombok.Data;

/**
 * 系统模块验证码配置。
 * <p>
 * 前后端交互使用的验证码配置对象，包含模式、图片验证码参数与滑块参数。
 */
@Data
public class CaptchaConfig {
    /** 验证码模式：image/slider/none */
    private String mode;
    /** 图片验证码参数配置 */
    private Image image;
    /** 滑块验证码参数配置 */
    private Slider slider;

    /** 图片验证码参数配置。 */
    @Data
    public static class Image {
        /** Redis 键前缀 */
        private String keyPrefix;
        /** 过期秒数 */
        private int expireSeconds;
        /** 图片宽度 */
        private int width;
        /** 图片高度 */
        private int height;
        /** 验证码长度 */
        private int length;
    }

    /** 滑块验证码参数配置。 */
    @Data
    public static class Slider {
        /** 是否开启二次校验 */
        private boolean secondaryEnabled;
        /** 主令牌键前缀 */
        private String keyPrefix;
        /** 次令牌键前缀 */
        private String secondaryKeyPrefix;
        /** 令牌过期秒数 */
        private int tokenExpireSeconds;
        /** 提供方（如 redis-token） */
        private String provider;
    }

    /**
     * 默认配置生成。
     * @return 默认验证码配置（模式 none，图片与滑块填充默认参数）
     */
    public static CaptchaConfig defaults() {
        CaptchaConfig c = new CaptchaConfig();
        c.setMode("none");
        Image img = new Image();
        img.setKeyPrefix("captcha:image");
        img.setExpireSeconds(120);
        img.setWidth(120);
        img.setHeight(40);
        img.setLength(4);
        c.setImage(img);
        Slider s = new Slider();
        s.setSecondaryEnabled(false);
        s.setKeyPrefix("captcha:slider");
        s.setSecondaryKeyPrefix("captcha:secondary");
        s.setTokenExpireSeconds(120);
        s.setProvider("redis-token");
        c.setSlider(s);
        return c;
    }
}

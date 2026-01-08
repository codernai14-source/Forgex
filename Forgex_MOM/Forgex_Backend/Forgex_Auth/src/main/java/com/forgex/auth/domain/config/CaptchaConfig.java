/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.auth.domain.config;

import lombok.Data;

/**
 * 验证码配置实体
 * 作用：承载单键 JSON 的验证码配置，用于控制验证码模式与具体参数；
 * 字段：包含顶层模式、图片参数、滑块参数三部分；通过 `ConfigService#getJson` 一次解析得到。
 */
@Data
public class CaptchaConfig {
    /** 验证码模式：none/image/slider */
    private String mode;
    /** 图片验证码配置 */
    private Image image;
    /** 滑块验证码配置 */
    private Slider slider;

    /** 图片验证码子配置 */
    @Data
    public static class Image {
        /** Redis键前缀 */
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

    /** 滑块验证码子配置 */
    @Data
    public static class Slider {
        /** 是否启用二次验证 */
        private boolean secondaryEnabled;
        /** 一次性令牌键前缀 */
        private String keyPrefix;
        /** 二次验证令牌键前缀 */
        private String secondaryKeyPrefix;
        /** 令牌过期秒数 */
        private int tokenExpireSeconds;
        /** 提供方标识（redis-token/third-party等） */
        private String provider;
    }

    /**
     * 构建默认配置
     * 逻辑：若系统未设置该键，使用默认参数保证功能可用
     * @return 默认配置实体
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

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
 * <p>
 * 作用：承载单键 JSON 的验证码配置，用于控制验证码模式与具体参数；
 * 字段：包含顶层模式、图片参数、滑块参数三部分；通过 {@code ConfigService#getJson} 一次解析得到。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.auth.service.CaptchaService
 * @see com.forgex.auth.service.impl.CaptchaServiceImpl
 */
@Data
public class CaptchaConfig {
    /**
     * 验证码模式：none/image/slider
     * <ul>
     *     <li>none：不启用验证码</li>
     *     <li>image：图片验证码</li>
     *     <li>slider：滑块验证码</li>
     * </ul>
     */
    private String mode;

    /**
     * 图片验证码配置
     * <p>包含图片验证码的 Redis 键前缀、过期时间、尺寸等参数</p>
     */
    private Image image;

    /**
     * 滑块验证码配置
     * <p>包含滑块验证码的 Redis 键前缀、令牌过期时间、提供方等参数</p>
     */
    private Slider slider;

    /**
     * 图片验证码子配置
     * <p>
     * 用于配置图片验证码的生成参数，包括尺寸、验证码长度、Redis 存储等
     * </p>
     */
    @Data
    public static class Image {
        /**
         * Redis 键前缀
         * <p>用于在 Redis 中存储验证码图片与答案的对应关系</p>
         */
        private String keyPrefix;

        /**
         * 过期秒数
         * <p>验证码在 Redis 中的有效期，超时后自动失效</p>
         */
        private int expireSeconds;

        /**
         * 图片宽度
         * <p>生成的验证码图片宽度（像素）</p>
         */
        private int width;

        /**
         * 图片高度
         * <p>生成的验证码图片高度（像素）</p>
         */
        private int height;

        /**
         * 验证码长度
         * <p>生成的验证码字符数量</p>
         */
        private int length;
    }

    /**
     * 滑块验证码子配置
     * <p>
     * 用于配置滑块验证码的行为参数，包括是否启用二次验证、令牌管理、提供方等
     * </p>
     */
    @Data
    public static class Slider {
        /**
         * 是否启用二次验证
         * <p>
         * true：滑块验证通过后需要再次验证（如短信验证码）<br>
         * false：滑块验证通过即可
         * </p>
         */
        private boolean secondaryEnabled;

        /**
         * 一次性令牌键前缀
         * <p>用于在 Redis 中存储滑块验证通过的一次性令牌</p>
         */
        private String keyPrefix;

        /**
         * 二次验证令牌键前缀
         * <p>当启用二次验证时，用于存储二次验证令牌的 Redis 键前缀</p>
         */
        private String secondaryKeyPrefix;

        /**
         * 令牌过期秒数
         * <p>滑块验证令牌在 Redis 中的有效期，超时后自动失效</p>
         */
        private int tokenExpireSeconds;

        /**
         * 提供方标识
         * <p>
         * 可选值：
         * <ul>
         *     <li>redis-token：使用 Redis 存储令牌（默认）</li>
         *     <li>third-party：使用第三方服务验证</li>
         * </ul>
         * </p>
         */
        private String provider;
    }

    /**
     * 构建默认配置
     * <p>
     * 逻辑：若系统未设置该键，使用默认参数保证功能可用
     * </p>
     *
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

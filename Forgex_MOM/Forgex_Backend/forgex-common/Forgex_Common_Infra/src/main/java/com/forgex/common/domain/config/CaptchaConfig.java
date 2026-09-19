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
package com.forgex.common.domain.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 验证码配置
 * <p>
 * 由系统配置库读取，控制登录流程的验证码模式与滑块参数。
 * 该配置用于定义登录页面的验证码类型和相关参数。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 */
@Setter
@Getter
public class CaptchaConfig {
    /**
     * 验证码模式
     * <p>支持 image（图片验证码）、slider（滑块验证码）、none（无验证码）。</p>
     */
    private String mode;
    
    /**
     * 滑块参数配置
     * <p>滑块验证码的相关参数配置。</p>
     */
    private SliderConfig slider;
    
    /**
     * 创建默认验证码配置
     *
     * @return 默认验证码配置对象，模式为 image，滑块禁用，令牌 120 秒过期
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
     * 滑块验证码参数配置
     * <p>用于配置滑块验证码的 Redis 键前缀、校验方式、令牌过期时间等参数。</p>
     */
    @Setter
    @Getter
    public static class SliderConfig {
        /**
         * 主令牌键前缀
         * <p>Redis 中存储主令牌的键前缀。</p>
         */
        private String keyPrefix;
        
        /**
         * 次令牌键前缀
         * <p>Redis 中存储次令牌的键前缀，用于二次校验。</p>
         */
        private String secondaryKeyPrefix;
        
        /**
         * 是否开启二次校验
         * <p>标识是否需要二次验证，提高安全性。</p>
         */
        private boolean secondaryEnabled;
        
        /**
         * 令牌过期秒数
         * <p>验证码令牌的有效期，单位为秒。</p>
         */
        private int tokenExpireSeconds;
    }
}


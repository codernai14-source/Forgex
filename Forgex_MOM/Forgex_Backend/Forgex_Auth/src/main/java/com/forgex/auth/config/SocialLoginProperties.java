package com.forgex.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 社交登录配置属性类
 * <p>配置微信和钉钉社交登录的相关参数。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "forgex.social")
public class SocialLoginProperties {

    /**
     * 回调基础URL
     */
    private String redirectBaseUrl;

    /**
     * 微信配置
     */
    private WeChat wechat = new WeChat();

    /**
     * 钉钉配置
     */
    private DingTalk dingtalk = new DingTalk();

    /**
     * 微信配置类
     */
    @Data
    public static class WeChat {
        /**
         * 微信AppID
         */
        private String appId;

        /**
         * 微信回调路径
         */
        private String callbackPath = "/api/auth/social/callback/wechat";
    }

    /**
     * 钉钉配置类
     */
    @Data
    public static class DingTalk {
        /**
         * 钉钉Client ID
         */
        private String clientId;

        /**
         * 钉钉回调路径
         */
        private String callbackPath = "/api/auth/social/callback/dingtalk";
    }
}


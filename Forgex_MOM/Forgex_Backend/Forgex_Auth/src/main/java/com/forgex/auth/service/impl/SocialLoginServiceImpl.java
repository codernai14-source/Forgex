package com.forgex.auth.service.impl;

import com.forgex.auth.config.SocialLoginProperties;
import com.forgex.auth.service.SocialLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 社交登录服务实现类
 * <p>实现社交登录功能，支持微信和钉钉登录。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SocialLoginServiceImpl implements SocialLoginService {

    /**
     * 社交登录配置
     */
    private final SocialLoginProperties properties;

    /**
     * 构建社交登录授权URL
     * <p>根据指定的平台生成对应的授权链接。</p>
     * 
     * @param platform 社交平台名称（支持：wechat、dingtalk）
     * @return 授权URL，不支持的平台返回null
     */
    @Override
    public String buildAuthorizeUrl(String platform) {
        if (!StringUtils.hasText(platform)) {
            return null;
        }
        if (properties == null || !StringUtils.hasText(properties.getRedirectBaseUrl())) {
            return null;
        }
        String p = platform.trim().toUpperCase();
        if ("WECHAT".equals(p)) {
            return buildWeChatAuthorizeUrl();
        }
        if ("DINGTALK".equals(p)) {
            return buildDingTalkAuthorizeUrl();
        }
        return null;
    }

    private String buildWeChatAuthorizeUrl() {
        SocialLoginProperties.WeChat weChat = properties.getWechat();
        if (weChat == null || !StringUtils.hasText(weChat.getAppId()) || !StringUtils.hasText(weChat.getCallbackPath())) {
            return null;
        }
        String redirectUri = properties.getRedirectBaseUrl() + weChat.getCallbackPath();
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String state = UUID.randomUUID().toString().replace("-", "");
        return "https://open.weixin.qq.com/connect/qrconnect"
                + "?appid=" + weChat.getAppId()
                + "&redirect_uri=" + encodedRedirect
                + "&response_type=code"
                + "&scope=snsapi_login"
                + "&state=" + state
                + "#wechat_redirect";
    }

    /**
     * 构建钉钉授权URL
     * 
     * @return 钉钉授权URL，配置不完整时返回null
     */
    private String buildDingTalkAuthorizeUrl() {
        SocialLoginProperties.DingTalk dingTalk = properties.getDingtalk();
        if (dingTalk == null || !StringUtils.hasText(dingTalk.getClientId()) || !StringUtils.hasText(dingTalk.getCallbackPath())) {
            return null;
        }
        String redirectUri = properties.getRedirectBaseUrl() + dingTalk.getCallbackPath();
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String state = UUID.randomUUID().toString().replace("-", "");
        return "https://login.dingtalk.com/oauth2/auth"
                + "?redirect_uri=" + encodedRedirect
                + "&response_type=code"
                + "&client_id=" + dingTalk.getClientId()
                + "&scope=openid"
                + "&state=" + state
                + "&prompt=consent";
    }
}

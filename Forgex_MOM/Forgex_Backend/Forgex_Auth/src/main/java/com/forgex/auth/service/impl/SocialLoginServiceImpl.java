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
 * <p>
 * 实现社交登录功能，支持微信和钉钉等第三方登录平台。
 * 负责构建各平台的 OAuth2 授权 URL，引导用户进行授权登录。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #buildAuthorizeUrl(String)} - 构建社交登录授权 URL</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 * @see com.forgex.auth.service.SocialLoginService
 * @see com.forgex.auth.config.SocialLoginProperties
 */
@Service
@RequiredArgsConstructor
public class SocialLoginServiceImpl implements SocialLoginService {

    /**
     * 社交登录配置，包含微信和钉钉的配置信息
     */
    private final SocialLoginProperties properties;

    /**
     * 构建社交登录授权 URL
     * <p>
     * 根据指定的平台生成对应的 OAuth2 授权链接。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>校验平台名称是否为空</li>
     *   <li>检查配置是否完整（回调地址等）</li>
     *   <li>根据平台类型构建对应的授权 URL</li>
     *   <li>生成随机 state 参数用于防止 CSRF 攻击</li>
     * </ol>
     *
     * @param platform 社交平台名称（支持：wechat、dingtalk，不区分大小写）
     * @return 授权 URL 字符串，配置不完整或不支持的平台返回 null
     * @see com.forgex.auth.config.SocialLoginProperties
     * @see #buildWeChatAuthorizeUrl()
     * @see #buildDingTalkAuthorizeUrl()
     */
    @Override
    public String buildAuthorizeUrl(String platform) {
        // 校验平台名称是否为空
        if (!StringUtils.hasText(platform)) {
            return null;
        }
        // 检查配置是否完整
        if (properties == null || !StringUtils.hasText(properties.getRedirectBaseUrl())) {
            return null;
        }
        // 转换为大写进行匹配
        String p = platform.trim().toUpperCase();
        // 微信平台
        if ("WECHAT".equals(p)) {
            return buildWeChatAuthorizeUrl();
        }
        // 钉钉平台
        if ("DINGTALK".equals(p)) {
            return buildDingTalkAuthorizeUrl();
        }
        return null;
    }

    /**
     * 构建微信授权 URL
     * <p>
     * 根据微信开放平台 OAuth2 协议构建授权链接。
     * </p>
     * <p>参数说明：</p>
     * <ul>
     *   <li>appid：微信应用的 AppID</li>
     *   <li>redirect_uri：回调地址，由配置文件的回调路径拼接</li>
     *   <li>response_type=code：授权码模式</li>
     *   <li>scope=snsapi_login：使用网站应用扫码登录</li>
     *   <li>state：随机生成的防 CSRF 攻击参数</li>
     * </ul>
     *
     * @return 微信授权 URL 字符串，配置不完整时返回 null
     * @see com.forgex.auth.config.SocialLoginProperties.WeChat
     * @see #buildAuthorizeUrl(String)
     */
    private String buildWeChatAuthorizeUrl() {
        // 获取微信配置
        SocialLoginProperties.WeChat weChat = properties.getWechat();
        // 校验配置完整性
        if (weChat == null || !StringUtils.hasText(weChat.getAppId()) || !StringUtils.hasText(weChat.getCallbackPath())) {
            return null;
        }
        // 拼接回调地址
        String redirectUri = properties.getRedirectBaseUrl() + weChat.getCallbackPath();
        // URL 编码回调地址
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        // 生成随机 state 参数
        String state = UUID.randomUUID().toString().replace("-", "");
        // 构建微信授权 URL
        return "https://open.weixin.qq.com/connect/qrconnect"
                + "?appid=" + weChat.getAppId()
                + "&redirect_uri=" + encodedRedirect
                + "&response_type=code"
                + "&scope=snsapi_login"
                + "&state=" + state
                + "#wechat_redirect";
    }

    /**
     * 构建钉钉授权 URL
     * <p>
     * 根据钉钉开放平台 OAuth2 协议构建授权链接。
     * </p>
     * <p>参数说明：</p>
     * <ul>
     *   <li>redirect_uri：回调地址，由配置文件的回调路径拼接</li>
     *   <li>response_type=code：授权码模式</li>
     *   <li>client_id：钉钉应用的 Client ID</li>
     *   <li>scope=openid：申请获取用户基本信息</li>
     *   <li>state：随机生成的防 CSRF 攻击参数</li>
     *   <li>prompt=consent：强制显示授权页面</li>
     * </ul>
     *
     * @return 钉钉授权 URL 字符串，配置不完整时返回 null
     * @see com.forgex.auth.config.SocialLoginProperties.DingTalk
     * @see #buildAuthorizeUrl(String)
     */
    private String buildDingTalkAuthorizeUrl() {
        // 获取钉钉配置
        SocialLoginProperties.DingTalk dingTalk = properties.getDingtalk();
        // 校验配置完整性
        if (dingTalk == null || !StringUtils.hasText(dingTalk.getClientId()) || !StringUtils.hasText(dingTalk.getCallbackPath())) {
            return null;
        }
        // 拼接回调地址
        String redirectUri = properties.getRedirectBaseUrl() + dingTalk.getCallbackPath();
        // URL 编码回调地址
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        // 生成随机 state 参数
        String state = UUID.randomUUID().toString().replace("-", "");
        // 构建钉钉授权 URL
        return "https://login.dingtalk.com/oauth2/auth"
                + "?redirect_uri=" + encodedRedirect
                + "&response_type=code"
                + "&client_id=" + dingTalk.getClientId()
                + "&scope=openid"
                + "&state=" + state
                + "&prompt=consent";
    }
}

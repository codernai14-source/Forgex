package com.forgex.auth.service;

/**
 * 社交登录服务接口
 * <p>
 * 提供社交登录相关的功能，支持微信和钉钉等第三方登录平台。
 * 负责构建各平台的 OAuth2 授权 URL，引导用户进行授权登录。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #buildAuthorizeUrl(String)} - 构建社交登录授权 URL</li>
 * </ul>
 * <p>支持的平台：</p>
 * <ul>
 *   <li>微信（WeChat）- 使用微信开放平台 OAuth2 授权</li>
 *   <li>钉钉（DingTalk）- 使用钉钉开放平台 OAuth2 授权</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 * @see com.forgex.auth.service.impl.SocialLoginServiceImpl
 * @see com.forgex.auth.config.SocialLoginProperties
 */
public interface SocialLoginService {
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
     * @see com.forgex.auth.service.impl.SocialLoginServiceImpl#buildWeChatAuthorizeUrl()
     * @see com.forgex.auth.service.impl.SocialLoginServiceImpl#buildDingTalkAuthorizeUrl()
     */
    String buildAuthorizeUrl(String platform);
}


package com.forgex.auth.service;

/**
 * 社交登录服务接口
 * <p>提供社交登录相关的功能，包括构建授权URL等。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
public interface SocialLoginService {
    /**
     * 构建社交登录授权URL
     * <p>根据指定的平台生成对应的授权链接。</p>
     * 
     * @param platform 社交平台名称（支持：wechat、dingtalk）
     * @return 授权URL，不支持的平台返回null
     */
    String buildAuthorizeUrl(String platform);
}


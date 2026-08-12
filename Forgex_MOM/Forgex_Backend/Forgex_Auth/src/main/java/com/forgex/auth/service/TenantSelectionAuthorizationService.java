package com.forgex.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 租户选择授权服务。
 * <p>
 * 已登录的同账号会话可以切换租户；首次建立会话时必须消费匹配的短期登录交互码。
 * </p>
 */
@Service
public class TenantSelectionAuthorizationService {

    private final LoginInteractionCodeService interactionCodeService;

    /**
     * 创建租户选择授权服务。
     *
     * @param interactionCodeService 登录交互码服务
     */
    public TenantSelectionAuthorizationService(LoginInteractionCodeService interactionCodeService) {
        this.interactionCodeService = interactionCodeService;
    }

    /**
     * 授权租户选择请求。
     *
     * @param userId 用户 ID
     * @param account 请求账号
     * @param loginTerminal 登录终端
     * @param interactionCode 短期登录交互码
     * @return 是否允许继续建立或切换租户会话
     */
    public boolean authorize(Long userId, String account, String loginTerminal, String interactionCode) {
        if (StpUtil.isLogin()) {
            String currentAccount = StpUtil.getLoginIdAsString();
            return StringUtils.hasText(currentAccount) && currentAccount.equals(account);
        }
        return interactionCodeService.consume(interactionCode, userId, account, loginTerminal) != null;
    }
}

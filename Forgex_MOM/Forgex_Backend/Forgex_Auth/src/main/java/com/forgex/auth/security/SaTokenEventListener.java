package com.forgex.auth.security;

import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.forgex.common.security.LogoutAuditService;
import com.forgex.common.security.LogoutReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 事件监听器（Auth 模块）。
 * <p>
 * 用于更严格地区分登出原因：
 * - 被踢下线（KICKOUT）
 * - 被顶下线（REPLACED）
 * - 被封禁导致下线（DISABLE）
 * </p>
 *
 * <p>
 * 注意：Token 超时无法在过期瞬间回调（通常在下一次访问时抛出未登录异常），
 * 本项目通过全局异常处理器触发 {@link LogoutAuditService} 回写 TIMEOUT。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see com.forgex.common.web.GlobalExceptionHandler
 */
@Component
@RequiredArgsConstructor
public class SaTokenEventListener implements SaTokenListener {

    private final LogoutAuditService logoutAuditService;


    @Override
    public void doLogin(String s, Object o, String s1, SaLoginParameter saLoginParameter) {

    }

    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        logoutAuditService.recordLogoutByToken(tokenValue, LogoutReason.MANUAL);
    }

    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        logoutAuditService.recordLogoutByToken(tokenValue, LogoutReason.KICKOUT);
    }

    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        logoutAuditService.recordLogoutByToken(tokenValue, LogoutReason.REPLACED);
    }

    @Override
    public void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {
    }

    @Override
    public void doUntieDisable(String loginType, Object loginId, String service) {
    }

    @Override
    public void doOpenSafe(String loginType, String tokenValue, String service, long safeTime) {
    }

    @Override
    public void doCloseSafe(String loginType, String tokenValue, String service) {
    }

    @Override
    public void doCreateSession(String id) {
    }

    @Override
    public void doLogoutSession(String id) {
    }

    @Override
    public void doRenewTimeout(String loginType, Object loginId, String tokenValue, long timeout) {
    }
}


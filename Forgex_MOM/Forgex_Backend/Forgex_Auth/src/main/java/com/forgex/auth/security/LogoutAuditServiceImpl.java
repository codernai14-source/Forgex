package com.forgex.auth.security;

import com.forgex.auth.service.LoginLogService;
import com.forgex.common.security.LogoutAuditService;
import com.forgex.common.security.LogoutReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登出审计服务实现（Auth 模块）。
 * <p>
 * 主要用于在 token 超时、主动登出、被踢下线等场景下，
 * 按 tokenValue 回写 {@code sys_login_log.logout_time/logout_reason}。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see com.forgex.common.web.GlobalExceptionHandler
 */
@Service
@RequiredArgsConstructor
public class LogoutAuditServiceImpl implements LogoutAuditService {

    private final LoginLogService loginLogService;

    /**
     * 按 token 回写登出信息。
     *
     * @param tokenValue   tokenValue
     * @param logoutReason 登出原因
     * @return 是否更新成功
     */
    @Override
    public boolean recordLogoutByToken(String tokenValue, LogoutReason logoutReason) {
        loginLogService.recordLogoutByToken(tokenValue, logoutReason);
        return true;
    }
}


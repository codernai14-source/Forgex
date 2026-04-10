package com.forgex.auth.security;

import com.forgex.auth.service.LoginLogService;
import com.forgex.common.security.LogoutAuditService;
import com.forgex.common.security.LogoutReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登出审计服务实现（Auth 模块）。
 * <p>
 * 实现 LogoutAuditService 接口，主要用于在 token 超时、主动登出、被踢下线等场景下，
 * 按 tokenValue 回写 {@code sys_login_log} 表的 logout_time 和 logout_reason 字段。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-08
 * @see LogoutAuditService
 * @see com.forgex.common.web.GlobalExceptionHandler
 */
@Service
@RequiredArgsConstructor
public class LogoutAuditServiceImpl implements LogoutAuditService {

    /**
     * 登录日志服务，用于记录和管理用户登录日志。
     */
    private final LoginLogService loginLogService;

    /**
     * 按 token 回写登出信息。
     * <p>
     * 委托给 LoginLogService 处理具体的登出日志记录逻辑，包括更新登出时间和登出原因。
     * </p>
     *
     * @param tokenValue   Token 值，不能为空
     * @param logoutReason 登出原因枚举，包括 MANUAL（主动登出）、KICKOUT（被踢）、REPLACED（被顶替）等
     * @return 是否更新成功，成功返回 true，失败返回 false
     * @see LogoutAuditService#recordLogoutByToken(String, LogoutReason)
     * @see LoginLogService#recordLogoutByToken(String, LogoutReason)
     */
    @Override
    public boolean recordLogoutByToken(String tokenValue, LogoutReason logoutReason) {
        // 委托给 LoginLogService 处理具体逻辑
        loginLogService.recordLogoutByToken(tokenValue, logoutReason);
        return true;
    }
}

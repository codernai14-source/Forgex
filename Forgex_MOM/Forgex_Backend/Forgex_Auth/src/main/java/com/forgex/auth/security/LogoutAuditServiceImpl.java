package com.forgex.auth.security;

import com.forgex.common.security.LogoutAuditService;
import com.forgex.common.security.LogoutReason;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 登录日志只记录登录事件，登出审计不再写回登录日志。
 * <p>
 * Auth 模块仍实现公共登出审计接口，用于兼容安全框架在主动登出、超时或踢下线时的调用链。
 * 当前策略为跳过登录日志回写，仅输出调试日志并允许登出流程继续。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-08
 * @see LogoutAuditService
 */
@Service
@Slf4j
public class LogoutAuditServiceImpl implements LogoutAuditService {

    /**
     * 按 token 记录登出审计。
     *
     * @param tokenValue   Token 值
     * @param logoutReason 登出原因
     * @return 固定返回 true，表示登出流程可继续
     */
    @Override
    public boolean recordLogoutByToken(String tokenValue, LogoutReason logoutReason) {
        // 登录日志已收敛为登录事件，不再记录登出时间和登出原因。
        log.debug("skip auth logout audit for login log, tokenValue={}, reason={}", tokenValue, logoutReason);
        return true;
    }
}

/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.auth.service.impl;

import com.forgex.auth.domain.entity.LoginLog;
import com.forgex.auth.mapper.LoginLogMapper;
import com.forgex.auth.service.LoginLogService;
import com.forgex.common.security.LogoutReason;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Auth 模块登录日志服务。
 * <p>
 * 提供异步登录成功和登录失败日志记录能力，日志写入失败时只记录错误日志，不阻断认证主流程。
 * 登出信息已从登录日志中剥离，{@link #recordLogoutByToken(String, LogoutReason)} 仅保留兼容实现。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2025-01-14
 * @see LoginLogService
 * @see LoginLog
 * @see LoginLogMapper
 */
@Slf4j
@Service
public class LoginLogServiceImpl implements LoginLogService {

    /**
     * 登录日志 Mapper。
     */
    @Autowired
    private LoginLogMapper loginLogMapper;

    /**
     * 记录登录成功日志。
     *
     * @param userId     用户 ID
     * @param account    登录账号
     * @param tenantId   租户 ID
     * @param ip         登录 IP
     * @param region     IP 归属地
     * @param userAgent  浏览器 User-Agent
     * @param tokenValue Token 值
     */
    @Async
    @Override
    public void recordLoginSuccess(Long userId, String account, Long tenantId,
                                   String ip, String region, String userAgent, String tokenValue) {
        try {
            // 登录日志异步写入，避免审计失败影响登录主流程。
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(userId);
            loginLog.setAccount(account);
            loginLog.setTenantId(tenantId);
            loginLog.setLoginIp(ip);
            loginLog.setLoginRegion(region);
            loginLog.setUserAgent(userAgent);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setStatus(1);
            loginLog.setTokenValue(tokenValue);
            loginLog.setCreateTime(LocalDateTime.now());
            loginLogMapper.insert(loginLog);
            log.info("record login success, account={}, ip={}, region={}", account, ip, region);
        } catch (Exception e) {
            log.error("record login success failed, account={}, ip={}", account, ip, e);
        }
    }

    /**
     * 记录登录失败日志。
     *
     * @param account   登录账号
     * @param tenantId  租户 ID
     * @param ip        登录 IP
     * @param region    IP 归属地
     * @param userAgent 浏览器 User-Agent
     * @param reason    失败原因
     */
    @Async
    @Override
    public void recordLoginFailure(String account, Long tenantId,
                                   String ip, String region, String userAgent, String reason) {
        try {
            // 失败日志同样异步写入，便于排查认证失败原因。
            LoginLog loginLog = new LoginLog();
            loginLog.setAccount(account);
            loginLog.setTenantId(tenantId);
            loginLog.setLoginIp(ip);
            loginLog.setLoginRegion(region);
            loginLog.setUserAgent(userAgent);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setStatus(0);
            loginLog.setReason(reason);
            loginLog.setCreateTime(LocalDateTime.now());
            loginLogMapper.insert(loginLog);
            log.info("record login failure, account={}, ip={}, reason={}", account, ip, reason);
        } catch (Exception e) {
            log.error("record login failure failed, account={}, ip={}", account, ip, e);
        }
    }

    /**
     * 兼容登出审计接口。
     * <p>
     * 当前登录日志不再记录登出时间和登出原因，因此仅输出调试日志。
     * </p>
     *
     * @param tokenValue   Token 值
     * @param logoutReason 登出原因
     */
    @Async
    @Override
    public void recordLogoutByToken(String tokenValue, LogoutReason logoutReason) {
        log.debug("skip recording logout info into login log, tokenValue={}, reason={}", tokenValue, logoutReason);
    }
}

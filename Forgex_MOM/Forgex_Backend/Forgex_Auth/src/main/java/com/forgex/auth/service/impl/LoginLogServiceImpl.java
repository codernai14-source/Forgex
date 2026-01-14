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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.security.LogoutReason;
import com.forgex.auth.domain.entity.LoginLog;
import com.forgex.auth.mapper.LoginLogMapper;
import com.forgex.auth.service.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录日志服务实现（Auth 模块）
 * <p>
 * 提供异步的登录日志记录功能，不阻塞登录流程。
 * 记录失败时仅记录错误日志，不影响业务流程。
 * </p>
 * 
 * @author coder_nai@163.com
 * @date 2025-01-14
 */
@Slf4j
@Service
public class LoginLogServiceImpl implements LoginLogService {
    
    @Autowired
    private LoginLogMapper loginLogMapper;
    
    /**
     * 记录登录成功日志（异步）
     * <p>
     * 使用 @Async 注解实现异步执行，不阻塞登录流程。
     * 记录失败时捕获异常并记录错误日志，不影响业务。
     * </p>
     * 
     * @param userId 用户ID
     * @param account 登录账号
     * @param tenantId 租户ID
     * @param ip 登录IP
     * @param region IP归属地
     * @param userAgent 浏览器UA
     */
    @Async
    @Override
    public void recordLoginSuccess(Long userId, String account, Long tenantId, 
                                   String ip, String region, String userAgent, String tokenValue) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(userId);
            loginLog.setAccount(account);
            loginLog.setTenantId(tenantId);
            loginLog.setLoginIp(ip);
            loginLog.setLoginRegion(region);
            loginLog.setUserAgent(userAgent);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setStatus(1); // 成功
            loginLog.setTokenValue(tokenValue);
            loginLog.setCreateTime(LocalDateTime.now());
            
            loginLogMapper.insert(loginLog);
            log.info("记录登录成功日志：账号={}, IP={}, 地区={}", account, ip, region);
        } catch (Exception e) {
            log.error("记录登录成功日志失败：账号={}, IP={}", account, ip, e);
        }
    }
    
    /**
     * 记录登录失败日志（异步）
     * <p>
     * 使用 @Async 注解实现异步执行，不阻塞登录流程。
     * 记录失败时捕获异常并记录错误日志，不影响业务。
     * </p>
     * 
     * @param account 登录账号
     * @param tenantId 租户ID
     * @param ip 登录IP
     * @param region IP归属地
     * @param userAgent 浏览器UA
     * @param reason 失败原因
     */
    @Async
    @Override
    public void recordLoginFailure(String account, Long tenantId, 
                                   String ip, String region, String userAgent, String reason) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setAccount(account);
            loginLog.setTenantId(tenantId);
            loginLog.setLoginIp(ip);
            loginLog.setLoginRegion(region);
            loginLog.setUserAgent(userAgent);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setStatus(0); // 失败
            loginLog.setReason(reason);
            loginLog.setCreateTime(LocalDateTime.now());
            
            loginLogMapper.insert(loginLog);
            log.info("记录登录失败日志：账号={}, IP={}, 原因={}", account, ip, reason);
        } catch (Exception e) {
            log.error("记录登录失败日志失败：账号={}, IP={}", account, ip, e);
        }
    }

    /**
     * 记录登出时间（异步）。
     * <p>
     * 逻辑：查询最近一条登录成功且 logoutTime 为空的日志，并更新其 logoutTime。
     * </p>
     *
     * @param userId   用户ID
     * @param account  登录账号
     * @param tenantId 租户ID
     */
    @Async
    @Override
    public void recordLogoutByToken(String tokenValue, LogoutReason logoutReason) {
        try {
            if (!org.springframework.util.StringUtils.hasText(tokenValue)) {
                return;
            }

            LoginLog last = loginLogMapper.selectOne(new LambdaQueryWrapper<LoginLog>()
                    .eq(LoginLog::getTokenValue, tokenValue)
                    .eq(LoginLog::getStatus, 1)
                    .isNull(LoginLog::getLogoutTime)
                    .orderByDesc(LoginLog::getLoginTime)
                    .last("limit 1"));
            if (last == null || last.getId() == null) {
                return;
            }

            LoginLog update = new LoginLog();
            update.setId(last.getId());
            update.setLogoutTime(LocalDateTime.now());
            update.setLogoutReason(logoutReason == null ? LogoutReason.UNKNOWN.name() : logoutReason.name());
            loginLogMapper.updateById(update);
            log.info("记录登出时间：tokenValue={}, reason={}, logId={}", tokenValue, update.getLogoutReason(), last.getId());
        } catch (Exception e) {
            log.error("记录登出时间失败：tokenValue={}", tokenValue, e);
        }
    }
}

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
 * 登录日志服务实现类（Auth 模块）
 * <p>
 * 提供异步的登录日志记录功能，不阻塞登录流程。
 * 记录失败时仅记录错误日志，不影响业务流程。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #recordLoginSuccess(Long, String, Long, String, String, String, String)} - 记录登录成功日志</li>
 *   <li>{@link #recordLoginFailure(String, Long, String, String, String, String)} - 记录登录失败日志</li>
 *   <li>{@link #recordLogoutByToken(String, com.forgex.common.security.LogoutReason)} - 记录登出时间</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2025-01-14
 * @see com.forgex.auth.service.LoginLogService
 * @see com.forgex.auth.domain.entity.LoginLog
 * @see com.forgex.auth.mapper.LoginLogMapper
 */
@Slf4j
@Service
public class LoginLogServiceImpl implements LoginLogService {
    
    /**
     * 登录日志 Mapper，用于操作登录日志数据
     */
    @Autowired
    private LoginLogMapper loginLogMapper;
    
    /**
     * 记录登录成功日志（异步）
     * <p>
     * 使用 @Async 注解实现异步执行，不阻塞登录流程。
     * 记录失败时捕获异常并记录错误日志，不影响业务。
     * </p>
     * <p>存储内容：</p>
     * <ul>
     *   <li>用户 ID、账号、租户 ID</li>
     *   <li>登录 IP、归属地、User-Agent</li>
     *   <li>Token 值（用于后续更新登出时间）</li>
     *   <li>登录时间、状态（1=成功）</li>
     * </ul>
     *
     * @param userId 用户 ID，不能为空
     * @param account 登录账号，不能为空
     * @param tenantId 租户 ID，不能为空
     * @param ip 登录 IP 地址，不能为空
     * @param region IP 归属地（省份 + 城市），可为空
     * @param userAgent 浏览器 User-Agent，可为空
     * @param tokenValue Token 值（用于回写登出时间），可为空
     * @see #recordLoginFailure(String, Long, String, String, String, String)
     * @see #recordLogoutByToken(String, com.forgex.common.security.LogoutReason)
     */
    @Async
    @Override
    public void recordLoginSuccess(Long userId, String account, Long tenantId,
                                   String ip, String region, String userAgent, String tokenValue) {
        try {
            // 创建登录日志对象
            LoginLog loginLog = new LoginLog();
            // 设置用户 ID
            loginLog.setUserId(userId);
            // 设置登录账号
            loginLog.setAccount(account);
            // 设置租户 ID
            loginLog.setTenantId(tenantId);
            // 设置登录 IP
            loginLog.setLoginIp(ip);
            // 设置登录地区
            loginLog.setLoginRegion(region);
            // 设置 User-Agent
            loginLog.setUserAgent(userAgent);
            // 设置登录时间
            loginLog.setLoginTime(LocalDateTime.now());
            // 设置状态（1=成功）
            loginLog.setStatus(1);
            // 设置 Token 值（用于后续更新登出时间）
            loginLog.setTokenValue(tokenValue);
            // 设置创建时间
            loginLog.setCreateTime(LocalDateTime.now());
            
            // 插入登录日志记录
            loginLogMapper.insert(loginLog);
            log.info("记录登录成功日志：账号={}, IP={}, 地区={}", account, ip, region);
        } catch (Exception e) {
            // 记录失败日志，不影响业务
            log.error("记录登录成功日志失败：账号={}, IP={}", account, ip, e);
        }
    }
    
    /**
     * 记录登录失败日志（异步）
     * <p>
     * 使用 @Async 注解实现异步执行，不阻塞登录流程。
     * 记录失败时捕获异常并记录错误日志，不影响业务。
     * </p>
     * <p>存储内容：</p>
     * <ul>
     *   <li>账号、租户 ID（可能为 0）</li>
     *   <li>登录 IP、归属地、User-Agent</li>
     *   <li>失败原因（如：密码错误、用户不存在等）</li>
     *   <li>登录时间、状态（0=失败）</li>
     * </ul>
     *
     * @param account 登录账号，不能为空
     * @param tenantId 租户 ID，登录失败时可能为 0
     * @param ip 登录 IP 地址，不能为空
     * @param region IP 归属地（省份 + 城市），可为空
     * @param userAgent 浏览器 User-Agent，可为空
     * @param reason 失败原因，不能为空
     * @see #recordLoginSuccess(Long, String, Long, String, String, String, String)
     */
    @Async
    @Override
    public void recordLoginFailure(String account, Long tenantId,
                                   String ip, String region, String userAgent, String reason) {
        try {
            // 创建登录日志对象
            LoginLog loginLog = new LoginLog();
            // 设置登录账号
            loginLog.setAccount(account);
            // 设置租户 ID
            loginLog.setTenantId(tenantId);
            // 设置登录 IP
            loginLog.setLoginIp(ip);
            // 设置登录地区
            loginLog.setLoginRegion(region);
            // 设置 User-Agent
            loginLog.setUserAgent(userAgent);
            // 设置登录时间
            loginLog.setLoginTime(LocalDateTime.now());
            // 设置状态（0=失败）
            loginLog.setStatus(0);
            // 设置失败原因
            loginLog.setReason(reason);
            // 设置创建时间
            loginLog.setCreateTime(LocalDateTime.now());
            
            // 插入登录日志记录
            loginLogMapper.insert(loginLog);
            log.info("记录登录失败日志：账号={}, IP={}, 原因={}", account, ip, reason);
        } catch (Exception e) {
            // 记录失败日志，不影响业务
            log.error("记录登录失败日志失败：账号={}, IP={}", account, ip, e);
        }
    }

    /**
     * 记录登出时间（异步）
     * <p>
     * 将指定 Token 对应的最近一次登录成功且未记录登出时间的日志更新为已登出。
     * 该方法异步执行，不影响登出主流程。
     * </p>
     * <p>处理逻辑：</p>
     * <ol>
     *   <li>根据 Token 值查询最近一条登录成功且 logoutTime 为空的记录</li>
     *   <li>更新其 logoutTime 为当前时间</li>
     *   <li>记录登出原因（如：手动登出、超时登出等）</li>
     * </ol>
     *
     * @param tokenValue Token 值，不能为空
     * @param logoutReason 登出原因，可为空
     * @see #recordLoginSuccess(Long, String, Long, String, String, String, String)
     * @see com.forgex.auth.service.impl.AuthServiceImpl#logout()
     * @see com.forgex.common.security.LogoutReason
     */
    @Async
    @Override
    public void recordLogoutByToken(String tokenValue, LogoutReason logoutReason) {
        try {
            // 校验 Token 值是否为空
            if (!org.springframework.util.StringUtils.hasText(tokenValue)) {
                return;
            }

            // 查询最近一条登录成功且未记录登出时间的日志
            LoginLog last = loginLogMapper.selectOne(new LambdaQueryWrapper<LoginLog>()
                    .eq(LoginLog::getTokenValue, tokenValue)
                    .eq(LoginLog::getStatus, 1)
                    .isNull(LoginLog::getLogoutTime)
                    .orderByDesc(LoginLog::getLoginTime)
                    .last("limit 1"));
            // 如果没有找到记录，直接返回
            if (last == null || last.getId() == null) {
                return;
            }

            // 创建更新对象
            LoginLog update = new LoginLog();
            // 设置日志 ID
            update.setId(last.getId());
            // 设置登出时间
            update.setLogoutTime(LocalDateTime.now());
            // 设置登出原因
            update.setLogoutReason(logoutReason == null ? LogoutReason.UNKNOWN.name() : logoutReason.name());
            // 更新登录日志
            loginLogMapper.updateById(update);
            log.info("记录登出时间：tokenValue={}, reason={}, logId={}", tokenValue, update.getLogoutReason(), last.getId());
        } catch (Exception e) {
            // 记录失败日志，不影响业务
            log.error("记录登出时间失败：tokenValue={}", tokenValue, e);
        }
    }
}

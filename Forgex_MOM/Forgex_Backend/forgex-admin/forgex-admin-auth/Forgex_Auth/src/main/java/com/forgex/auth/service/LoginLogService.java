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
package com.forgex.auth.service;

/**
 * 登录日志服务接口（Auth 模块）
 * <p>
 * 用于在认证模块中记录用户登录行为，包括成功和失败的登录尝试。
 * 所有记录操作均为异步执行，不阻塞登录流程。
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
 * @see com.forgex.auth.service.impl.LoginLogServiceImpl
 * @see com.forgex.auth.domain.entity.LoginLog
 */
public interface LoginLogService {
    
    /**
     * 记录登录成功日志（异步）
     * <p>
     * 该方法异步执行，不会阻塞登录流程。记录失败时仅记录错误日志，不影响业务。
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
    void recordLoginSuccess(Long userId, String account, Long tenantId,
                           String ip, String region, String userAgent, String tokenValue);
    
    /**
     * 记录登录失败日志（异步）
     * <p>
     * 该方法异步执行，不会阻塞登录流程。记录失败时仅记录错误日志，不影响业务。
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
    void recordLoginFailure(String account, Long tenantId,
                           String ip, String region, String userAgent, String reason);

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
    void recordLogoutByToken(String tokenValue, com.forgex.common.security.LogoutReason logoutReason);
}

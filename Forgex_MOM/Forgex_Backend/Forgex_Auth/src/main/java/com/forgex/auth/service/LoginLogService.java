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
 * 
 * @author coder_nai@163.com
 * @date 2025-01-14
 */
public interface LoginLogService {
    
    /**
     * 记录登录成功日志（异步）
     * <p>
     * 该方法异步执行，不会阻塞登录流程。记录失败时仅记录错误日志，不影响业务。
     * </p>
     * 
     * @param userId 用户ID，不能为空
     * @param account 登录账号，不能为空
     * @param tenantId 租户ID，不能为空
     * @param ip 登录IP地址，不能为空
     * @param region IP归属地（省份+城市），可为空
     * @param userAgent 浏览器User-Agent，可为空
     * @param tokenValue tokenValue（用于回写登出时间），可为空
     */
    void recordLoginSuccess(Long userId, String account, Long tenantId, 
                           String ip, String region, String userAgent, String tokenValue);
    
    /**
     * 记录登录失败日志（异步）
     * <p>
     * 该方法异步执行，不会阻塞登录流程。记录失败时仅记录错误日志，不影响业务。
     * </p>
     * 
     * @param account 登录账号，不能为空
     * @param tenantId 租户ID，登录失败时可能为0
     * @param ip 登录IP地址，不能为空
     * @param region IP归属地（省份+城市），可为空
     * @param userAgent 浏览器User-Agent，可为空
     * @param reason 失败原因，不能为空
     */
    void recordLoginFailure(String account, Long tenantId, 
                           String ip, String region, String userAgent, String reason);

    /**
     * 记录登出时间（异步）。
     * <p>
     * 将指定用户在指定租户下“最近一次登录成功且未记录登出时间”的日志更新为已登出。
     * 该方法异步执行，不影响登出主流程。
     * </p>
     *
     * @param userId   用户ID
     * @param account  登录账号
     * @param tenantId 租户ID
     * @see com.forgex.auth.service.impl.AuthServiceImpl#logout()
     */
    void recordLogoutByToken(String tokenValue, com.forgex.common.security.LogoutReason logoutReason);
}

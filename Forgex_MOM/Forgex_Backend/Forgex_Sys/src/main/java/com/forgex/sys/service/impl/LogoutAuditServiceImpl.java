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
package com.forgex.sys.service.impl;

import com.forgex.common.security.LogoutAuditService;
import com.forgex.common.security.LogoutReason;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 登录日志已收缩为“仅登录日志”，登出事件不再回写到登录日志。
 * <p>
 * Sys 模块保留该实现用于兼容公共登出审计接口，避免登出流程因为缺少实现而失败。
 * 当前业务约定是登录日志只记录登录事件，登出事件仅输出调试日志。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2026-01-27
 * @see LogoutAuditService
 */
@Slf4j
@Service
public class LogoutAuditServiceImpl implements LogoutAuditService {

    /**
     * 按 token 记录登出审计。
     * <p>
     * 当前实现不再回写登录日志，仅保持接口幂等返回，避免影响登出主流程。
     * </p>
     *
     * @param tokenValue   Token 值
     * @param logoutReason 登出原因
     * @return 固定返回 true，表示登出流程可继续
     */
    @Async
    @Override
    public boolean recordLogoutByToken(String tokenValue, LogoutReason logoutReason) {
        // 登录日志表只保留登录事件，登出信息不再写入该表。
        log.debug("skip logout audit for login log, tokenValue={}, reason={}", tokenValue, logoutReason);
        return true;
    }
}

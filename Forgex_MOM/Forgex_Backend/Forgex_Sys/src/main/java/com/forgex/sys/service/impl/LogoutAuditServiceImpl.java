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

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.forgex.common.security.LogoutAuditService;
import com.forgex.common.security.LogoutReason;
import com.forgex.sys.domain.entity.LoginLog;
import com.forgex.sys.mapper.LoginLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 登出审计服务实现。
 * <p>
 * 用于记录用户登出信息，包括登出时间和登出原因。
 * 通过 tokenValue 定位到对应的登录日志记录并更新。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2026-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutAuditServiceImpl implements LogoutAuditService {

    private final LoginLogMapper loginLogMapper;

    /**
     * 按 token 回写登出信息（异步执行）。
     * <p>
     * 根据 tokenValue 查找对应的登录日志记录，更新登出时间和登出原因。
     * 如果找不到对应的记录，则记录警告日志。
     * </p>
     *
     * @param tokenValue   tokenValue
     * @param logoutReason 登出原因
     * @return 是否更新成功
     */
    @Async
    @Override
    public boolean recordLogoutByToken(String tokenValue, LogoutReason logoutReason) {
        if (!StringUtils.hasText(tokenValue)) {
            log.warn("记录登出日志失败：tokenValue 为空");
            return false;
        }

        if (logoutReason == null) {
            logoutReason = LogoutReason.UNKNOWN;
        }

        try {
            // 构建更新条件：根据 tokenValue 查找登录日志
            LambdaUpdateWrapper<LoginLog> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(LoginLog::getTokenValue, tokenValue)
                    .isNull(LoginLog::getLogoutTime); // 只更新还未记录登出时间的记录

            // 设置更新字段
            updateWrapper.set(LoginLog::getLogoutTime, LocalDateTime.now())
                    .set(LoginLog::getLogoutReason, logoutReason.name());

            // 执行更新
            int updateCount = loginLogMapper.update(null, updateWrapper);

            if (updateCount > 0) {
                log.info("记录登出日志成功：tokenValue={}, 登出原因={}, 更新记录数={}", 
                        maskToken(tokenValue), logoutReason.name(), updateCount);
                return true;
            } else {
                log.warn("记录登出日志失败：未找到对应的登录记录，tokenValue={}", maskToken(tokenValue));
                return false;
            }
        } catch (Exception e) {
            log.error("记录登出日志异常：tokenValue={}, 登出原因={}", 
                    maskToken(tokenValue), logoutReason.name(), e);
            return false;
        }
    }

    /**
     * 脱敏 token（只显示前后各4位）。
     *
     * @param token token
     * @return 脱敏后的 token
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "****";
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}


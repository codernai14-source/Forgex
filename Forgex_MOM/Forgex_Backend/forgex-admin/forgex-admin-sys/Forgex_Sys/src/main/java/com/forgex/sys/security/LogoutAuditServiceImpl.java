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
package com.forgex.sys.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.security.LogoutAuditService;
import com.forgex.common.security.LogoutReason;
import com.forgex.sys.domain.entity.LoginLog;
import com.forgex.sys.mapper.LoginLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Sys 模块登出审计服务。
 * <p>
 * 实现公共接口 {@link LogoutAuditService}，供踢下线、会话超时、全局异常处理等场景
 * 按 token 回写 {@code sys_login_log.logout_time} 与 {@code logout_reason}。
 * 登录日志位于 history 数据源，访问入口为 {@link LoginLogMapper}。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see LogoutAuditService
 * @see LogoutReason
 * @see LoginLog
 * @see LoginLogMapper
 */
@Service
@RequiredArgsConstructor
public class LogoutAuditServiceImpl implements LogoutAuditService {

    /**
     * 登录日志 Mapper，绑定 history 数据源上的 {@code sys_login_log}。
     */
    private final LoginLogMapper loginLogMapper;

    /**
     * 按 token 回写最近一次成功登录且尚未登出的记录。
     * <p>
     * token 为空、找不到对应登录记录或记录已写过登出时间时返回 {@code false}，
     * 不抛异常，避免阻断踢下线或超时清理主流程。
     * </p>
     *
     * @param tokenValue   会话 token 值，不能为空
     * @param logoutReason 登出原因；为空时回写 {@link LogoutReason#UNKNOWN}
     * @return {@code true} 表示已更新登录日志；{@code false} 表示无需或无法回写
     * @see LoginLog#getTokenValue()
     * @see LoginLog#getLogoutTime()
     */
    @Override
    public boolean recordLogoutByToken(String tokenValue, LogoutReason logoutReason) {
        // 参数校验：缺少 token 时无法定位登录会话
        if (!StringUtils.hasText(tokenValue)) {
            return false;
        }

        // 查询该 token 最近一次成功且尚未登出的登录日志
        LoginLog last = loginLogMapper.selectOne(new LambdaQueryWrapper<LoginLog>()
                .eq(LoginLog::getTokenValue, tokenValue)
                .eq(LoginLog::getStatus, 1)
                .isNull(LoginLog::getLogoutTime)
                .orderByDesc(LoginLog::getLoginTime)
                .last("limit 1"));
        if (last == null || last.getId() == null) {
            return false;
        }

        // 回写登出时间和原因，保持登录记录可审计
        LoginLog update = new LoginLog();
        update.setId(last.getId());
        update.setLogoutTime(LocalDateTime.now());
        update.setLogoutReason(logoutReason == null ? LogoutReason.UNKNOWN.name() : logoutReason.name());
        loginLogMapper.updateById(update);
        return true;
    }
}

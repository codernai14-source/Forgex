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
 * 登出审计服务实现（Sys 模块）。
 * <p>
 * 用于在系统管理模块中触发“踢下线”等操作时，
 * 回写 {@code sys_login_log.logout_time/logout_reason}。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class LogoutAuditServiceImpl implements LogoutAuditService {

    private final LoginLogMapper loginLogMapper;

    /**
     * 按 token 回写登出信息。
     *
     * @param tokenValue   tokenValue
     * @param logoutReason 登出原因
     * @return 是否更新成功
     */
    @Override
    public boolean recordLogoutByToken(String tokenValue, LogoutReason logoutReason) {
        if (!StringUtils.hasText(tokenValue)) {
            return false;
        }

        LoginLog last = loginLogMapper.selectOne(new LambdaQueryWrapper<LoginLog>()
                .eq(LoginLog::getTokenValue, tokenValue)
                .eq(LoginLog::getStatus, 1)
                .isNull(LoginLog::getLogoutTime)
                .orderByDesc(LoginLog::getLoginTime)
                .last("limit 1"));
        if (last == null || last.getId() == null) {
            return false;
        }

        LoginLog update = new LoginLog();
        update.setId(last.getId());
        update.setLogoutTime(LocalDateTime.now());
        update.setLogoutReason(logoutReason == null ? LogoutReason.UNKNOWN.name() : logoutReason.name());
        loginLogMapper.updateById(update);
        return true;
    }
}


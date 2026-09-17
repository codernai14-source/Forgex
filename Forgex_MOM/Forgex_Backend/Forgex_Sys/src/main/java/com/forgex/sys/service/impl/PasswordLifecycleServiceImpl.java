package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserPasswordHistory;
import com.forgex.sys.mapper.SysUserPasswordHistoryMapper;
import com.forgex.sys.service.IPasswordLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 口令历史复用检测实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class PasswordLifecycleServiceImpl implements IPasswordLifecycleService {

    private final SysUserPasswordHistoryMapper historyMapper;

    /**
     * 校验未复用并写入历史。
     *
     * @param user        用户
     * @param rawPassword 新明文
     * @param provider    校验器
     * @param policy      策略
     * @return true 表示通过
     */
    @Override
    public boolean assertNotReusedAndRecord(SysUser user, String rawPassword, CryptoPasswordProvider provider, PasswordPolicyConfig policy) {
        int historyCount = policy == null || policy.getHistoryCount() == null ? 5 : policy.getHistoryCount();
        if (historyCount > 0 && provider != null && StringUtils.hasText(user.getPassword())
                && provider.verify(rawPassword, user.getPassword())) {
            return false;
        }
        if (historyCount > 0) {
            List<SysUserPasswordHistory> histories = historyMapper.selectList(new LambdaQueryWrapper<SysUserPasswordHistory>()
                    .eq(SysUserPasswordHistory::getUserId, user.getId())
                    .orderByDesc(SysUserPasswordHistory::getCreatedTime)
                    .last("limit " + historyCount));
            for (SysUserPasswordHistory history : histories) {
                if (provider != null && provider.verify(rawPassword, history.getPasswordHash())) {
                    return false;
                }
            }
        }
        if (StringUtils.hasText(user.getPassword())) {
            SysUserPasswordHistory history = new SysUserPasswordHistory();
            history.setTenantId(user.getTenantId() == null ? 0L : user.getTenantId());
            history.setUserId(user.getId());
            history.setPasswordHash(user.getPassword());
            history.setCreatedTime(LocalDateTime.now());
            historyMapper.insert(history);
        }
        return true;
    }
}

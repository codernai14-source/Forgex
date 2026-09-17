package com.forgex.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.auth.domain.entity.SysUser;
import com.forgex.auth.domain.entity.SysUserPasswordHistory;
import com.forgex.auth.mapper.SysUserMapper;
import com.forgex.auth.mapper.SysUserPasswordHistoryMapper;
import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.crypto.CryptoProviders;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.common.security.compliance.ComplianceContext;
import com.forgex.common.security.compliance.ComplianceLevel;
import com.forgex.common.security.password.PasswordPolicyValidator;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Auth 侧口令生命周期辅助。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class PasswordLifecycleHelper {

    private static final String TICKET_PREFIX = "fx:auth:pwd:ticket:";

    private final SysUserPasswordHistoryMapper historyMapper;
    private final SysUserMapper userMapper;
    private final RedissonClient redissonClient;

    /**
     * 判断是否需要强制改密。
     *
     * @param user   用户
     * @param policy 策略
     * @return true 表示需要
     */
    public boolean requiresChange(SysUser user, PasswordPolicyConfig policy) {
        if (Boolean.TRUE.equals(user.getMustChangePwd())) {
            return true;
        }
        if (policy == null || !Boolean.TRUE.equals(policy.getExpireEnabled()) || policy.getExpireDays() == null || policy.getExpireDays() <= 0) {
            return false;
        }
        LocalDateTime updated = user.getPwdUpdateTime() != null ? user.getPwdUpdateTime() : user.getCreateTime();
        return updated != null && updated.plusDays(policy.getExpireDays()).isBefore(LocalDateTime.now());
    }

    public boolean isExpired(SysUser user, PasswordPolicyConfig policy) {
        if (user == null || policy == null || !Boolean.TRUE.equals(policy.getExpireEnabled())
                || policy.getExpireDays() == null || policy.getExpireDays() <= 0) return false;
        LocalDateTime updated = user.getPwdUpdateTime() != null ? user.getPwdUpdateTime() : user.getCreateTime();
        return updated != null && updated.plusDays(policy.getExpireDays()).isBefore(LocalDateTime.now());
    }

    public Integer expireInDays(SysUser user, PasswordPolicyConfig policy) {
        if (user == null || policy == null || !Boolean.TRUE.equals(policy.getExpireEnabled())
                || policy.getExpireDays() == null || policy.getExpireDays() <= 0) return null;
        LocalDateTime updated = user.getPwdUpdateTime() != null ? user.getPwdUpdateTime() : user.getCreateTime();
        if (updated == null) return null;
        long days = java.time.Duration.between(LocalDateTime.now(), updated.plusDays(policy.getExpireDays())).toDays();
        Integer warn = policy.getExpireWarnDays() == null ? 7 : Math.max(0, policy.getExpireWarnDays());
        return days >= 0 && days <= warn ? Math.toIntExact(days) : null;
    }

    /**
     * 校验新口令不可复用并写入历史。
     *
     * @param user     用户
     * @param newHash  新哈希
     * @param provider 校验器
     * @param policy   策略
     * @return true 表示未复用
     */
    public boolean assertNotReusedAndRecord(SysUser user, String rawPassword, String newHash,
                                            CryptoPasswordProvider provider, PasswordPolicyConfig policy) {
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
            history.setTenantId(0L);
            history.setUserId(user.getId());
            history.setPasswordHash(user.getPassword());
            history.setCreatedTime(LocalDateTime.now());
            historyMapper.insert(history);
        }
        return true;
    }

    /**
     * 更新口令并清除强制改密标记。
     *
     * @param userId 用户 ID
     * @param hash   新哈希
     */
    public void markPasswordUpdated(Long userId, String hash) {
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(hash);
        update.setPwdUpdateTime(LocalDateTime.now());
        update.setMustChangePwd(false);
        userMapper.updateById(update);
    }

    /**
     * 校验策略。
     *
     * @param password 明文
     * @param account  账号
     * @param policy   策略
     * @return true 表示通过
     */
    public boolean validPolicy(String password, String account, PasswordPolicyConfig policy) {
        return PasswordPolicyValidator.isValid(password, account, policy);
    }

    /**
     * 签发强制改密票据。
     *
     * @param userId  用户 ID
     * @param account 账号
     * @return 票据
     */
    public String issueTicket(Long userId, String account) {
        String ticket = java.util.UUID.randomUUID().toString().replace("-", "");
        redissonClient.getBucket(TICKET_PREFIX + ticket).set(userId + ":" + account, java.time.Duration.ofMinutes(5));
        return ticket;
    }

    /**
     * 消费强制改密票据。
     *
     * @param ticket 票据
     * @return userId:account，失效时返回 null
     */
    public String consumeTicket(String ticket) {
        if (!StringUtils.hasText(ticket)) {
            return null;
        }
        RBucket<String> bucket = redissonClient.getBucket(TICKET_PREFIX + ticket);
        String payload = bucket.get();
        if (payload != null) {
            bucket.delete();
        }
        return payload;
    }
}

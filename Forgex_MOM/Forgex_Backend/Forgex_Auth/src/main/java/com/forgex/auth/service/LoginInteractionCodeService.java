package com.forgex.auth.service;

import com.forgex.auth.domain.dto.LoginInteractionContext;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.HexFormat;

/**
 * Redis 登录交互码服务。
 * <p>
 * 交互码仅用于从凭据校验阶段过渡到首次租户选择阶段。交互码绑定用户、账号和终端，
 * 五分钟内有效，并通过 Redis CAS 原子删除保证只能成功消费一次。
 * </p>
 */
@Service
public class LoginInteractionCodeService {

    private static final String KEY_PREFIX = "fx:auth:login:interaction:";
    private static final long EXPIRE_SECONDS = 300L;

    private final RedissonClient redissonClient;

    /**
     * 创建登录交互码服务。
     *
     * @param redissonClient Redisson 客户端
     */
    public LoginInteractionCodeService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 发放短期登录交互码。
     *
     * @param userId 用户 ID
     * @param account 登录账号
     * @param loginTerminal 登录终端
     * @return 不透明交互码
     */
    public String issue(Long userId, String account, String loginTerminal) {
        String code = UUID.randomUUID().toString();
        LoginInteractionContext context = LoginInteractionContext.of(userId, account, loginTerminal);
        redissonClient.<String>getBucket(buildKey(code))
                .set(context.toJson(), EXPIRE_SECONDS, TimeUnit.SECONDS);
        return code;
    }

    /**
     * 校验并原子消费登录交互码。
     *
     * @param code 交互码
     * @param userId 预期用户 ID
     * @param account 预期账号
     * @param loginTerminal 预期登录终端
     * @return 消费成功时返回绑定上下文，否则返回 {@code null}
     */
    public LoginInteractionContext consume(String code, Long userId, String account, String loginTerminal) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        RBucket<String> bucket = redissonClient.getBucket(buildKey(code));
        String payload = bucket.get();
        if (!StringUtils.hasText(payload)) {
            return null;
        }

        LoginInteractionContext context;
        try {
            context = LoginInteractionContext.fromJson(payload);
        } catch (RuntimeException ex) {
            return null;
        }
        if (!Objects.equals(userId, context.userId())
                || !Objects.equals(account, context.account())
                || !Objects.equals(loginTerminal, context.loginTerminal())) {
            return null;
        }
        return bucket.compareAndSet(payload, null) ? context : null;
    }

    private String buildKey(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(code.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}

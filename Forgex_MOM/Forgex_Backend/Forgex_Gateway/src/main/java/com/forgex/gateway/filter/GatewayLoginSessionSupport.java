package com.forgex.gateway.filter;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 网关登录会话支持类。
 * <p>
 * 统一负责以下能力：
 * 1. 从请求 Cookie 中读取当前 Sa-Token；
 * 2. 基于 Sa-Token 会话解析当前登录用户上下文；
 * 3. 刷新在线用户缓存 TTL，使其与 Sa-Token 有效期保持一致。
 * </p>
 * <p>
 * 在线用户 key 的过期时间调整使用 {@link RedissonClient} 原生 API，
 * 避免通过 {@code StringRedisTemplate} 走 Spring Data Redis 的 {@code RedisConnection} 适配层。
 * 在部分 Redisson + spring-data-redis 组合下，适配层可能对 {@code pExpire} 形成错误委托链，
 * 从而触发 {@code StackOverflowError}（见网关日志中的 {@code DefaultedRedisConnection#pExpire} 自递归）。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.1
 * @see RedissonClient
 */
@Component
public class GatewayLoginSessionSupport {

    public static final String LOGIN_CONTEXT_ATTRIBUTE = GatewayLoginSessionSupport.class.getName() + ".LOGIN_CONTEXT";

    private static final String ONLINE_USER_PREFIX = "fx:online:user:";
    private static final String SESSION_USER_ID = "LOGIN_USER_ID";
    private static final String SESSION_TENANT_ID = "LOGIN_TENANT_ID";
    private static final String SESSION_ACCOUNT = "LOGIN_ACCOUNT";

    /**
     * Redisson 客户端，用于在线用户缓存 TTL 的原子更新。
     */
    private final RedissonClient redissonClient;

    /**
     * 构造方法。
     *
     * @param redissonClient Redisson 客户端，由 {@code redisson-spring-boot-starter} 注入
     */
    public GatewayLoginSessionSupport(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 从当前请求中解析登录上下文。
     *
     * @param request 当前请求
     * @return 登录上下文，未登录时返回 null
     */
    public LoginSessionContext resolve(ServerHttpRequest request) {
        return resolveByToken(resolveToken(request));
    }

    /**
     * 从当前请求中解析 token。
     *
     * @param request 当前请求
     * @return token 值，未找到时返回 null
     */
    public String resolveToken(ServerHttpRequest request) {
        if (request == null || request.getCookies() == null) {
            return null;
        }
        String tokenName = StpUtil.getTokenName();
        if (!StringUtils.hasText(tokenName)) {
            return null;
        }
        HttpCookie cookie = request.getCookies().getFirst(tokenName);
        return normalizeToken(cookie == null ? null : cookie.getValue());
    }

    /**
     * 基于 token 解析登录上下文。
     *
     * @param token token 值
     * @return 登录上下文，token 无效时返回 null
     */
    public LoginSessionContext resolveByToken(String token) {
        String tokenValue = normalizeToken(token);
        if (!StringUtils.hasText(tokenValue)) {
            return null;
        }

        Object loginId;
        try {
            loginId = StpUtil.getLoginIdByToken(tokenValue);
        } catch (Exception ignored) {
            return null;
        }
        if (loginId == null) {
            return null;
        }

        try {
            StpUtil.getStpLogic().updateLastActiveToNow(tokenValue);
        } catch (Exception ignored) {
        }

        SaSession session;
        try {
            session = StpUtil.getSessionByLoginId(loginId, false);
        } catch (Exception ignored) {
            return null;
        }
        if (session == null) {
            return null;
        }

        Long userId = parseLong(session.get(SESSION_USER_ID));
        Long tenantId = parseLong(session.get(SESSION_TENANT_ID));
        String account = parseText(session.get(SESSION_ACCOUNT));
        if (!StringUtils.hasText(account) && StringUtils.hasText(String.valueOf(loginId))) {
            account = String.valueOf(loginId);
        }
        if (userId == null && tenantId == null && !StringUtils.hasText(account)) {
            return null;
        }

        return new LoginSessionContext(tokenValue, loginId, userId, tenantId, account);
    }

    /**
     * 刷新在线用户缓存 TTL。
     * <p>
     * 使用 Redisson {@link RBucket} 设置过期或清除过期，与 {@link #resolveEffectiveTtlSeconds(String)} 计算结果对齐。
     * </p>
     *
     * @param context 登录上下文；若缺少用户、租户或 token 则直接返回
     */
    public void refreshOnlineUserTtl(LoginSessionContext context) {
        if (context == null || context.userId == null || context.tenantId == null || !StringUtils.hasText(context.token)) {
            return;
        }

        try {
            Long ttlSeconds = resolveEffectiveTtlSeconds(context.token);
            String onlineKey = ONLINE_USER_PREFIX + context.tenantId + ":" + context.userId;

            // 使用 Redisson 原生 API，避免 Spring Data Redis 连接适配层在 pExpire 上的错误委托
            RBucket<Object> bucket = redissonClient.getBucket(onlineKey);

            if (ttlSeconds == null) {
                // 与 Redis PERSIST 语义一致：取消该 key 的过期时间
                bucket.clearExpire();
                return;
            }
            if (ttlSeconds > 0) {
                bucket.expire(Duration.ofSeconds(ttlSeconds));
                return;
            }
            bucket.delete();
        } catch (Exception ignored) {
        }
    }

    /**
     * 计算 token 当前可用的有效 TTL。
     *
     * @param token token 值
     * @return 有效 TTL 秒数，永久有效时返回 null
     */
    public Long resolveEffectiveTtlSeconds(String token) {
        String tokenValue = normalizeToken(token);
        if (!StringUtils.hasText(tokenValue)) {
            return null;
        }

        Long tokenTimeout = normalizeTimeout(readTokenTimeout(tokenValue));
        Long activeTimeout = normalizeTimeout(readActiveTimeout(tokenValue));
        if (tokenTimeout == null) {
            return activeTimeout;
        }
        if (activeTimeout == null) {
            return tokenTimeout;
        }
        return Math.min(tokenTimeout, activeTimeout);
    }

    private long readTokenTimeout(String token) {
        try {
            return StpUtil.getTokenTimeout(token);
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private long readActiveTimeout(String token) {
        try {
            return StpUtil.getStpLogic().getTokenActiveTimeoutByToken(token);
        } catch (Exception ignored) {
            return SaTokenDao.NOT_VALUE_EXPIRE;
        }
    }

    private Long normalizeTimeout(long seconds) {
        if (seconds == SaTokenDao.NOT_VALUE_EXPIRE || seconds == SaTokenDao.NEVER_EXPIRE) {
            return null;
        }
        if (seconds <= 0) {
            return 0L;
        }
        return seconds;
    }

    private String normalizeToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        String value = token.trim();
        return StringUtils.hasText(value) ? value : null;
    }

    private Long parseLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            try {
                return Long.valueOf(text);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private String parseText(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return StringUtils.hasText(text) ? text : null;
    }

    /**
     * 登录上下文。
     */
    public static final class LoginSessionContext {
        private final String token;
        private final Object loginId;
        private final Long userId;
        private final Long tenantId;
        private final String account;

        public LoginSessionContext(String token, Object loginId, Long userId, Long tenantId, String account) {
            this.token = token;
            this.loginId = loginId;
            this.userId = userId;
            this.tenantId = tenantId;
            this.account = account;
        }

        public String getToken() {
            return token;
        }

        public Object getLoginId() {
            return loginId;
        }

        public Long getUserId() {
            return userId;
        }

        public Long getTenantId() {
            return tenantId;
        }

        public String getAccount() {
            return account;
        }
    }
}

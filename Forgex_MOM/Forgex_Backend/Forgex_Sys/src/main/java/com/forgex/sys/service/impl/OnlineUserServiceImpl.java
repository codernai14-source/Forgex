package com.forgex.sys.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.security.LogoutAuditService;
import com.forgex.common.security.LogoutReason;
import com.forgex.common.tenant.TenantContext;
import com.forgex.sys.domain.dto.OnlineUserQueryDTO;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.vo.OnlineUserVO;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.service.IOnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 在线用户服务实现。
 * <p>
 * 当前实现基于 Redis 中的登录上下文 Key：{@code fx:login:ctx:{token}}，
 * 按租户过滤并分页返回在线会话列表，同时关联 {@code sys_user} 的最后登录信息用于展示。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see com.forgex.auth.service.impl.AuthServiceImpl#chooseTenant(com.forgex.auth.domain.param.TenantChoiceParam)
 */
@Service
@RequiredArgsConstructor
public class OnlineUserServiceImpl implements IOnlineUserService {

    private static final String LOGIN_CTX_PREFIX = "fx:login:ctx:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SysUserMapper userMapper;
    private final LogoutAuditService logoutAuditService;

    /**
     * 分页查询在线用户。
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 在线用户分页结果
     */
    @Override
    public IPage<OnlineUserVO> pageOnlineUsers(Page<OnlineUserVO> page, OnlineUserQueryDTO query) {
        // 1) 优先使用线程上下文中的租户，保证租户隔离
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            tenantId = query == null ? null : query.getTenantId();
        }
        if (tenantId == null) {
            page.setTotal(0);
            page.setRecords(Collections.emptyList());
            return page;
        }

        // 2) 扫描登录上下文 Key（fx:login:ctx:*）
        Set<String> keys = redisTemplate.keys(LOGIN_CTX_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            page.setTotal(0);
            page.setRecords(Collections.emptyList());
            return page;
        }

        String accountLike = query == null ? null : query.getAccount();

        List<LoginCtx> ctxList = new ArrayList<>();
        for (String key : keys) {
            String json = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                continue;
            }
            LoginCtx ctx = parseLoginCtx(json);
            if (ctx == null) {
                continue;
            }
            // 3) 按租户过滤，避免跨租户泄露
            if (ctx.tenantId == null || !tenantId.equals(ctx.tenantId)) {
                continue;
            }
            // 4) 按账号模糊过滤（可选）
            if (StringUtils.hasText(accountLike) && (ctx.account == null || !ctx.account.contains(accountLike))) {
                continue;
            }
            ctx.token = key.substring(LOGIN_CTX_PREFIX.length());
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            ctx.ttlSeconds = ttl == null ? -1L : ttl;
            ctxList.add(ctx);
        }

        if (ctxList.isEmpty()) {
            page.setTotal(0);
            page.setRecords(Collections.emptyList());
            return page;
        }

        // 5) 关联查询用户最后登录信息（用于页面展示）
        List<Long> userIds = ctxList.stream().map(c -> c.userId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<SysUser> users = userIds.isEmpty()
                ? Collections.emptyList()
                : userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getId, userIds));

        java.util.Map<Long, SysUser> userMap = users.stream().collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        List<OnlineUserVO> all = ctxList.stream().map(ctx -> {
            OnlineUserVO vo = new OnlineUserVO();
            vo.setToken(ctx.token);
            vo.setUserId(ctx.userId);
            vo.setTenantId(ctx.tenantId);
            vo.setAccount(ctx.account);
            vo.setTtlSeconds(ctx.ttlSeconds);
            SysUser u = ctx.userId == null ? null : userMap.get(ctx.userId);
            if (u != null) {
                vo.setLastLoginTime(u.getLastLoginTime());
                vo.setLastLoginIp(u.getLastLoginIp());
                vo.setLastLoginRegion(u.getLastLoginRegion());
            }
            return vo;
        }).collect(Collectors.toList());

        long total = all.size();
        long current = page.getCurrent() <= 0 ? 1 : page.getCurrent();
        long size = page.getSize() <= 0 ? 20 : page.getSize();
        int fromIndex = (int) Math.min(total, (current - 1) * size);
        int toIndex = (int) Math.min(total, fromIndex + size);
        List<OnlineUserVO> records = fromIndex >= toIndex ? Collections.emptyList() : all.subList(fromIndex, toIndex);

        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    /**
     * 统计指定租户在线用户数。
     *
     * @param tenantId 租户ID
     * @return 在线用户数
     */
    @Override
    public Long countOnlineUsers(Long tenantId) {
        if (tenantId == null) {
            return 0L;
        }
        Set<String> keys = redisTemplate.keys(LOGIN_CTX_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        long count = 0;
        for (String key : keys) {
            String json = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                continue;
            }
            LoginCtx ctx = parseLoginCtx(json);
            if (ctx != null && tenantId.equals(ctx.tenantId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 强制下线指定 token。
     *
     * @param token tokenValue
     * @return 是否成功
     * @see cn.dev33.satoken.stp.StpUtil#logoutByTokenValue(String)
     */
    @Override
    public boolean kickout(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            StpUtil.logoutByTokenValue(token);
        } catch (Exception ignored) {
        }
        try {
            // 回写登录日志登出原因（踢下线）
            logoutAuditService.recordLogoutByToken(token, LogoutReason.KICKOUT);
        } catch (Exception ignored) {
        }
        try {
            redisTemplate.delete(LOGIN_CTX_PREFIX + token);
        } catch (Exception ignored) {
        }
        return true;
    }

    /**
     * 解析登录上下文 JSON。
     *
     * @param json 登录上下文 JSON 字符串
     * @return 解析后的上下文；解析失败返回 null
     */
    private LoginCtx parseLoginCtx(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            LoginCtx ctx = new LoginCtx();
            JsonNode userIdNode = node.get("userId");
            JsonNode tenantIdNode = node.get("tenantId");
            JsonNode accountNode = node.get("account");
            if (userIdNode != null && userIdNode.isNumber()) {
                ctx.userId = userIdNode.longValue();
            } else if (userIdNode != null && userIdNode.isTextual()) {
                try {
                    ctx.userId = Long.valueOf(userIdNode.asText());
                } catch (Exception ignored) {
                }
            }
            if (tenantIdNode != null && tenantIdNode.isNumber()) {
                ctx.tenantId = tenantIdNode.longValue();
            } else if (tenantIdNode != null && tenantIdNode.isTextual()) {
                try {
                    ctx.tenantId = Long.valueOf(tenantIdNode.asText());
                } catch (Exception ignored) {
                }
            }
            if (accountNode != null && accountNode.isTextual()) {
                ctx.account = accountNode.asText();
            }
            return ctx;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 登录上下文内部对象。
     * <p>
     * 与 Redis 中 {@code fx:login:ctx:{token}} 的 JSON 字段保持一致。
     * </p>
     */
    private static class LoginCtx {
        private String token;
        private Long userId;
        private Long tenantId;
        private String account;
        private Long ttlSeconds;
    }
}

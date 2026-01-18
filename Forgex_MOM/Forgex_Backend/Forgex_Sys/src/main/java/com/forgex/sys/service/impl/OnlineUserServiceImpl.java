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
 * 当前实现基于 Redis 中的在线用户 Key：{@code fx:online:user:{tenantId}:{userId}}，
 * 按租户过滤并分页返回在线用户列表，同时关联 {@code sys_user} 的最后登录信息用于展示。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see com.forgex.auth.service.impl.AuthServiceImpl#chooseTenant(com.forgex.auth.domain.param.TenantChoiceParam)
 */
@Service
@RequiredArgsConstructor
public class OnlineUserServiceImpl implements IOnlineUserService {

    private static final String ONLINE_USER_PREFIX = "fx:online:user:";

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

        // 2) 扫描在线用户 Key（fx:online:user:{tenantId}:*）
        Set<String> keys = redisTemplate.keys(ONLINE_USER_PREFIX + tenantId + ":*");
        if (keys == null || keys.isEmpty()) {
            page.setTotal(0);
            page.setRecords(Collections.emptyList());
            return page;
        }

        String accountLike = query == null ? null : query.getAccount();

        List<OnlineUserInfo> userInfoList = new ArrayList<>();
        for (String key : keys) {
            String json = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                continue;
            }
            OnlineUserInfo userInfo = parseOnlineUserInfo(json);
            if (userInfo == null) {
                continue;
            }
            // 3) 按账号模糊过滤（可选）
            if (StringUtils.hasText(accountLike) && (userInfo.account == null || !userInfo.account.contains(accountLike))) {
                continue;
            }
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            userInfo.ttlSeconds = ttl == null ? -1L : ttl;
            userInfoList.add(userInfo);
        }

        if (userInfoList.isEmpty()) {
            page.setTotal(0);
            page.setRecords(Collections.emptyList());
            return page;
        }

        // 4) 关联查询用户最后登录信息（用于页面展示）
        List<Long> userIds = userInfoList.stream().map(u -> u.userId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<SysUser> users = userIds.isEmpty()
                ? Collections.emptyList()
                : userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getId, userIds));

        java.util.Map<Long, SysUser> userMap = users.stream().collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        List<OnlineUserVO> all = userInfoList.stream().map(userInfo -> {
            OnlineUserVO vo = new OnlineUserVO();
            vo.setToken(userInfo.token);
            vo.setUserId(userInfo.userId);
            vo.setTenantId(userInfo.tenantId);
            vo.setAccount(userInfo.account);
            vo.setTtlSeconds(userInfo.ttlSeconds);
            SysUser u = userInfo.userId == null ? null : userMap.get(userInfo.userId);
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
        Set<String> keys = redisTemplate.keys(ONLINE_USER_PREFIX + tenantId + ":*");
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return (long) keys.size();
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
            // 删除登录上下文
            redisTemplate.delete(ONLINE_USER_PREFIX + token);
        } catch (Exception ignored) {
        }
        try {
            // 删除在线用户缓存
            Set<String> keys = redisTemplate.keys(ONLINE_USER_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    String json = redisTemplate.opsForValue().get(key);
                    if (StringUtils.hasText(json)) {
                        OnlineUserInfo userInfo = parseOnlineUserInfo(json);
                        if (userInfo != null && token.equals(userInfo.token)) {
                            redisTemplate.delete(key);
                            break;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    /**
     * 解析在线用户信息 JSON。
     *
     * @param json 在线用户信息 JSON 字符串
     * @return 解析后的用户信息；解析失败返回 null
     */
    private OnlineUserInfo parseOnlineUserInfo(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            OnlineUserInfo userInfo = new OnlineUserInfo();
            JsonNode userIdNode = node.get("userId");
            JsonNode tenantIdNode = node.get("tenantId");
            JsonNode accountNode = node.get("account");
            JsonNode tokenNode = node.get("token");
            if (userIdNode != null && userIdNode.isNumber()) {
                userInfo.userId = userIdNode.longValue();
            } else if (userIdNode != null && userIdNode.isTextual()) {
                try {
                    userInfo.userId = Long.valueOf(userIdNode.asText());
                } catch (Exception ignored) {
                }
            }
            if (tenantIdNode != null && tenantIdNode.isNumber()) {
                userInfo.tenantId = tenantIdNode.longValue();
            } else if (tenantIdNode != null && tenantIdNode.isTextual()) {
                try {
                    userInfo.tenantId = Long.valueOf(tenantIdNode.asText());
                } catch (Exception ignored) {
                }
            }
            if (accountNode != null && accountNode.isTextual()) {
                userInfo.account = accountNode.asText();
            }
            if (tokenNode != null && tokenNode.isTextual()) {
                userInfo.token = tokenNode.asText();
            }
            return userInfo;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 在线用户信息内部对象。
     * <p>
     * 与 Redis 中 {@code fx:online:user:{tenantId}:{userId}} 的 JSON 字段保持一致。
     * </p>
     */
    private static class OnlineUserInfo {
        private String token;
        private Long userId;
        private Long tenantId;
        private String account;
        private Long ttlSeconds;
    }
}

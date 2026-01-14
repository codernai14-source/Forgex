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
package com.forgex.auth.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.auth.domain.entity.SysTenant;
import com.forgex.auth.domain.entity.SysUser;
import com.forgex.auth.domain.entity.SysUserTenant;
import com.forgex.auth.domain.param.LoginParam;
import com.forgex.auth.domain.dto.SysUserDTO;
import com.forgex.auth.domain.param.TenantChoiceParam;
import com.forgex.auth.domain.vo.TenantVO;
import com.forgex.auth.mapper.SysTenantMapper;
import com.forgex.auth.mapper.SysUserMapper;
import com.forgex.auth.mapper.SysUserTenantMapper;
import com.forgex.auth.service.AuthService;
import com.forgex.common.config.ConfigService;
import com.forgex.auth.service.CaptchaService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baomidou.dynamic.datasource.annotation.DS;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.core.util.HexUtil;
import java.nio.charset.StandardCharsets;
import com.forgex.common.domain.config.CaptchaConfig;
import com.forgex.common.domain.config.CryptoTransportConfig;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.Duration;
import java.time.LocalDateTime;
import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.crypto.CryptoProviders;

/**
 * 认证服务实现。
 * <p>
 * 负责登录流程与租户选择、验证码校验、密码校验等逻辑，数据源为 admin 库。
 * <p>
 * 使用：
 * - {@link #login(LoginParam)} 登录并返回可选租户列表；
 * - {@link #chooseTenant(TenantChoiceParam)} 选择默认租户；
 * - 按配置读取 {@code security.crypto.transport} 执行 SM2 传输解密，再按 {@code security.password.policy.store} 进行存储校验。
 * <p>
 * 可扩展性：
 * - 支持在配置库扩展验证码模式与密码策略；
 * - 新增密码算法时在 common 模块的 Provider 中扩展。
 */
@Service
@DS("admin")
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    @Autowired
    private SysTenantMapper tenantMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysUserTenantMapper userTenantMapper;
    @Autowired
    private ConfigService configService;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private LoginLogService loginLogService;
    @Autowired
    private com.forgex.common.util.IpLocationService ipLocationService;


    /**
     * 登录并返回租户列表。
     * <p>
     * 流程：
     * 1) 参数校验；2) 传输解密（若启用 SM2）；3) 用户存在性校验；
     * 4) 依据存储策略对密码校验；5) 验证码校验（按配置）；6) 登录并返回租户列表。
     * @param param 登录参数（可能为 SM2 加密密码）
     * @return 租户 VO 列表
     */
    @Override
    public R<List<TenantVO>> login(LoginParam param) {
        String account = param == null ? null : param.getAccount();
        String password = param == null ? null : param.getPassword();
        
        // 获取客户端IP和User-Agent
        String clientIp = getClientIp();
        String userAgent = getUserAgent();
        String region = ipLocationService.getLocationByIp(clientIp);
        
        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            log.warn("登录失败: 账号或密码为空");
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "账号或密码为空");
            return R.fail(500, "账号或密码不能为空");
        }
        // 如果启用了传输加密（SM2），优先尝试解密入参密码
        CryptoTransportConfig cryptoCfg = configService.getJson("security.crypto.transport", CryptoTransportConfig.class, null);
        if (cryptoCfg != null && StringUtils.hasText(cryptoCfg.getPrivateKey()) && "SM2".equalsIgnoreCase(cryptoCfg.getAlgorithm())) {
            try {
                SM2 sm2 = new SM2(cryptoCfg.getPrivateKey(), cryptoCfg.getPublicKey());
                String cipherFmt = cryptoCfg.getCipher();
                if ("BCD".equalsIgnoreCase(cipherFmt)) {
                    byte[] plain = sm2.decryptFromBcd(password, KeyType.PrivateKey);
                    password = new String(plain, StandardCharsets.UTF_8);
                } else {
                    password = sm2.decryptStr(password, KeyType.PrivateKey);
                }
            } catch (Exception ignored) {}
        }
        // 查询用户信息
        String idKey = account;
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId, SysUser::getAccount, SysUser::getUsername, SysUser::getPassword, SysUser::getEmail, SysUser::getPhone, SysUser::getStatus)
                .eq(SysUser::getAccount, idKey));
            if (user == null) {
                log.warn("登录失败: 用户不存在, account={}", idKey);
                loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "用户不存在");
                return R.fail(500, "用户不存在");
            }
        // 验证密码：根据策略执行（sm2 可解密存储 / bcrypt 哈希）
        PasswordPolicyConfig policy = configService.getJson("security.password.policy", PasswordPolicyConfig.class, null);
        String store = policy == null ? "bcrypt" : policy.getStore();
        CryptoPasswordProvider provider = CryptoProviders.resolve(store, configService);
        boolean passOk = provider.verify(password, user.getPassword());
        if (!passOk) {
            log.warn("登录失败: 密码不正确, account={}", account);
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "密码不正确");
            return R.fail(500, "密码不正确");
        }
        // 验证码校验（由配置决定方式）
        CaptchaConfig cfg = configService.getJson("login.captcha", CaptchaConfig.class, CaptchaConfig.defaults());
        String mode = cfg.getMode();
        if ("image".equalsIgnoreCase(mode)) {
            String captchaId = param.getCaptchaId();
            String captcha = param.getCaptcha();
            if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captcha)) {
                log.warn("登录失败: 图片验证码缺失, account={}", account);
                loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "验证码缺失");
                return R.fail(500, "验证码不能为空");
            }
            if (!captchaService.verifyImage(captchaId, captcha)) {
                log.warn("登录失败: 图片验证码错误, account={}", account);
                loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "验证码错误");
                return R.fail(500, "验证码不正确");
            }
        } else if ("slider".equalsIgnoreCase(mode)) {
            String token = param.getCaptcha();
            if (!StringUtils.hasText(token)) {
                log.warn("登录失败: 滑块令牌缺失, account={}", account);
                loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "验证码缺失");
                return R.fail(500, "验证码不能为空");
            }
            if (!captchaService.verifySlider(token)) {
                log.warn("登录失败: 滑块令牌校验失败, account={}", account);
                loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "验证码错误");
                return R.fail(500, "验证码不正确");
            }
        }
        // 查询用户绑定的租户ID列表
        List<SysUserTenant> binds = userTenantMapper.selectList(new LambdaQueryWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, user.getId())
                .orderByDesc(SysUserTenant::getPrefOrder)
                .orderByDesc(SysUserTenant::getLastUsed));
        List<Long> tenantIds = new ArrayList<>();
        for (SysUserTenant b : binds) tenantIds.add(b.getTenantId());
        if (tenantIds.isEmpty()) {
            log.warn("登录提示: 用户未绑定任何租户, account={}", account);
            return R.ok(Collections.emptyList());
        }
        // 查询对应租户信息
        List<SysTenant> tenants = tenantMapper.selectList(new LambdaQueryWrapper<SysTenant>()
                .in(SysTenant::getId, tenantIds));
        List<TenantVO> vos = new ArrayList<>();
        for (SysTenant t : tenants) {
            TenantVO vo = new TenantVO();
            vo.setId(t.getId() == null ? null : String.valueOf(t.getId()));
            vo.setName(t.getTenantName());
            vo.setIntro(t.getDescription());
            vo.setLogo(t.getLogo());
            vo.setTenantType(t.getTenantType());
            // 设置默认标记
            for (SysUserTenant b : binds) { if (b.getTenantId().equals(t.getId())) { vo.setIsDefault(b.getIsDefault()); break; } }
            vos.add(vo);
        }
        log.info("预登录成功（身份校验通过）: account={}, tenants={}", idKey, vos.size());
        return R.ok(vos);
    }

    /**
     * 选择租户，设置上下文
     * 逻辑：校验参数 -> 设置租户上下文
     * @param param 选择租户参数
     * @return 选择结果
     * @see com.forgex.common.tenant.TenantContext#set(Long)
     */
    @Override
    public R<SysUserDTO> chooseTenant(TenantChoiceParam param) {
        Long tenantId = param == null ? null : param.getTenantId();
        String account = param == null ? null : param.getAccount();
        if (tenantId == null) {
            return R.fail(500, "租户ID不能为空");
        }
        // 检查租户绑定关系
        if (!StringUtils.hasText(account)) return R.fail(500, "账号不能为空");
        String idKey = account;
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, idKey));
        if (user == null) return R.fail(500, "用户不存在");
        SysUserTenant bind = userTenantMapper.selectOne(new LambdaQueryWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, user.getId())
                .eq(SysUserTenant::getTenantId, tenantId)
                .last("limit 1"));
        if (bind == null) return R.fail(500, "未绑定该租户");
        StpUtil.login(idKey);
        SaSession session = StpUtil.getSession();
        if (session != null) {
            session.set("LOGIN_USER_ID", user.getId());
            session.set("LOGIN_TENANT_ID", tenantId);
            session.set("LOGIN_ACCOUNT", idKey);
        }
        String token = StpUtil.getTokenValue();
        if (StringUtils.hasText(token)) {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("userId", user.getId());
            ctx.put("tenantId", tenantId);
            ctx.put("account", idKey);
            String json = JSONUtil.toJsonStr(ctx);
            long timeout = StpUtil.getTokenTimeout();
            String key = "fx:login:ctx:" + token;
            if (timeout > 0) {
                redis.opsForValue().set(key, json, Duration.ofSeconds(timeout));
            } else {
                redis.opsForValue().set(key, json);
            }
        }
        TenantContext.set(tenantId);
        userTenantMapper.update(null, new LambdaUpdateWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, user.getId())
                .eq(SysUserTenant::getTenantId, tenantId)
                .set(SysUserTenant::getLastUsed, LocalDateTime.now())
                .setSql("pref_order = pref_order + 1"));
        
        // 记录登录成功日志
        String clientIp = getClientIp();
        String userAgent = getUserAgent();
        String region = ipLocationService.getLocationByIp(clientIp);
        loginLogService.recordLoginSuccess(user.getId(), account, tenantId, clientIp, region, userAgent, token);
        
        // 更新用户最后登录信息
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getLastLoginIp, clientIp)
                .set(SysUser::getLastLoginRegion, region)
                .set(SysUser::getLastLoginTime, LocalDateTime.now()));
        
        log.info("选择租户成功: account={}, tenantId={}", account, tenantId);
        SysUserDTO result = new SysUserDTO();
        result.setId(user.getId());
        result.setAccount(user.getAccount());
        result.setUsername(user.getUsername());
        result.setEmail(user.getEmail());
        result.setPhone(user.getPhone());
        result.setAvatar(user.getAvatar());
        result.setStatus(user.getStatus());
        result.setTenantId(bind.getTenantId());
        return R.ok(result);
    }

    /**
     * 更新租户偏好设置
     * <p>用于更新用户的租户排序和默认租户设置。</p>
     * <p>参数说明：</p>
     * <ul>
     *   <li>account：用户账号</li>
     *   <li>ordered：租户ID排序列表</li>
     *   <li>defaultTenantId：默认租户ID</li>
     * </ul>
     * @param account 用户账号
     * @param ordered 租户ID排序列表
     * @param defaultTenantId 默认租户ID
     * @return 更新结果
     */
    @Override
    public R<Boolean> updateTenantPreferences(String account, java.util.List<Long> ordered, Long defaultTenantId) {
        if (!StringUtils.hasText(account) || ordered == null) return R.fail(500, "参数错误");
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getAccount, account));
        if (user == null) return R.fail(500, "用户不存在");
        int n = ordered.size(); int weight = n;
        for (Long tid : ordered) {
            userTenantMapper.update(null, new LambdaUpdateWrapper<SysUserTenant>()
                    .eq(SysUserTenant::getUserId, user.getId())
                    .eq(SysUserTenant::getTenantId, tid)
                    .set(SysUserTenant::getPrefOrder, weight)
                    .set(SysUserTenant::getIsDefault, (defaultTenantId != null && defaultTenantId.equals(tid))));
            weight--;
        }
        return R.ok(true);
    }


    /**
     * 管理员权限校验
     * 逻辑：检查是否拥有 admin 角色
     * @return 校验结果
     * @see cn.dev33.satoken.stp.StpUtil#checkRole(String)
     */
    @Override
    public R<Boolean> secureAdmin() {
        StpUtil.checkRole("admin");
        return R.ok(true);
    }

    /**
     * 根据用户ID重置密码
     * <p>将指定用户的密码重置为默认密码 "123456"，使用 BCrypt 算法进行哈希存储。</p>
     * @param userId 用户ID
     * @return 重置结果
     */
    @Override
    public R<Boolean> resetPasswordById(Long userId) {
        if (userId == null) {
            return R.fail(500, "用户ID不能为空");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return R.fail(500, "用户不存在");
        }
        String hashed = BCrypt.hashpw("123456");
        int n = userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getPassword, hashed)
                .eq(SysUser::getId, userId));
        if (n > 0) {
            return R.ok(true);
        }
        return R.fail(500, "重置失败");
    }

    /**
     * 用户登出
     * <p>清除用户登录状态，删除Redis中的登录上下文信息，并移除租户上下文。</p>
     * @return 登出结果
     * @see StpUtil#logout()
     */
    @Override
    public R<Boolean> logout() {
        try {
            Object uid = StpUtil.getLoginIdDefaultNull();
            SaSession session = StpUtil.getSession(false);
            Long userId = null;
            Long tenantId = null;
            if (session != null) {
                Object uidObj = session.get("LOGIN_USER_ID");
                if (uidObj instanceof Long) {
                    userId = (Long) uidObj;
                } else if (uidObj instanceof Integer) {
                    userId = ((Integer) uidObj).longValue();
                } else if (uidObj instanceof String) {
                    try { userId = Long.valueOf((String) uidObj); } catch (Exception ignored) { }
                }
                Object tidObj = session.get("LOGIN_TENANT_ID");
                if (tidObj instanceof Long) {
                    tenantId = (Long) tidObj;
                } else if (tidObj instanceof Integer) {
                    tenantId = ((Integer) tidObj).longValue();
                } else if (tidObj instanceof String) {
                    try { tenantId = Long.valueOf((String) tidObj); } catch (Exception ignored) { }
                }
            }
            String account = uid == null ? null : String.valueOf(uid);

            // 记录登出时间/原因（异步，不影响主流程）
            String tokenValue = StpUtil.getTokenValue();
            loginLogService.recordLogoutByToken(tokenValue, com.forgex.common.security.LogoutReason.MANUAL);

            String token = tokenValue;
            if (StringUtils.hasText(tokenValue)) {
                String key = "fx:login:ctx:" + tokenValue;
                try {
                    redis.delete(key);
                } catch (Exception ignored) {
                }
            }
            StpUtil.logout();
            log.info("退出成功: account={}", uid);
            return R.ok(true);
        } catch (Exception e) {
            return R.fail(500, "退出失败");
        }
    }
    
    /**
     * 获取客户端真实IP
     * <p>
     * 从请求头 X-Client-IP 获取（由网关透传）。
     * 如果获取失败，返回 "unknown"。
     * </p>
     * 
     * @return 客户端IP地址
     */
    private String getClientIp() {
        try {
            org.springframework.web.context.request.RequestAttributes attrs = 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                jakarta.servlet.http.HttpServletRequest request = 
                    ((org.springframework.web.context.request.ServletRequestAttributes) attrs).getRequest();
                String ip = request.getHeader("X-Client-IP");
                if (StringUtils.hasText(ip)) {
                    return ip;
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
        }
        return "unknown";
    }
    
    /**
     * 获取User-Agent
     * <p>
     * 从请求头获取浏览器User-Agent信息。
     * 如果获取失败，返回 "unknown"。
     * </p>
     * 
     * @return User-Agent字符串
     */
    private String getUserAgent() {
        try {
            org.springframework.web.context.request.RequestAttributes attrs = 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                jakarta.servlet.http.HttpServletRequest request = 
                    ((org.springframework.web.context.request.ServletRequestAttributes) attrs).getRequest();
                String ua = request.getHeader("User-Agent");
                if (StringUtils.hasText(ua)) {
                    return ua;
                }
            }
        } catch (Exception e) {
            log.warn("获取User-Agent失败", e);
        }
        return "unknown";
    }
}

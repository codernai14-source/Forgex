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

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.auth.domain.entity.SysTenant;
import com.forgex.auth.domain.entity.SysUser;
import com.forgex.auth.domain.entity.SysRolePerm;
import com.forgex.auth.domain.entity.SysUserRolePerm;
import com.forgex.auth.domain.entity.SysUserTenant;
import com.forgex.auth.domain.param.LoginParam;
import com.forgex.auth.domain.dto.SysUserDTO;
import com.forgex.auth.domain.param.TenantChoiceParam;
import com.forgex.auth.domain.vo.TenantVO;
import com.forgex.auth.mapper.SysRolePermMapper;
import com.forgex.auth.mapper.SysTenantMapper;
import com.forgex.auth.mapper.SysUserMapper;
import com.forgex.auth.mapper.SysUserRolePermMapper;
import com.forgex.auth.mapper.SysUserTenantMapper;
import com.forgex.auth.mapper.SysInviteCodeMapper;
import com.forgex.auth.mapper.SysInviteRegisterRecordMapper;
import com.forgex.auth.service.AuthService;
import com.forgex.auth.service.LoginLogService;
import com.forgex.auth.strategy.AuthTerminalConstants;
import com.forgex.auth.strategy.LoginTypeConstants;
import com.forgex.auth.strategy.captcha.CaptchaStrategyFactory;
import com.forgex.auth.strategy.captcha.CaptchaValidationResult;
import com.forgex.auth.strategy.login.LoginStrategyFactory;
import com.forgex.auth.strategy.tenant.ChooseTenantStrategyFactory;
import com.forgex.common.config.ConfigService;
import com.forgex.auth.service.CaptchaService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.LoginSessionKeys;
import com.forgex.common.security.LoginSessionSupport;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.TenantContextIgnore;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;

import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.json.JSONUtil;
import java.nio.charset.StandardCharsets;
import com.forgex.common.domain.config.CaptchaConfig;
import com.forgex.common.domain.config.CryptoTransportConfig;
import com.forgex.common.domain.config.LoginSecurityConfig;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.Duration;
import java.time.LocalDateTime;
import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.auth.enums.AuthPromptEnum;
import com.forgex.common.crypto.CryptoProviders;
import com.forgex.common.enums.UserSourceEnum;

/**
 * 认证服务实现类
 * <p>
 * 负责处理用户登录、租户选择、安全校验、密码重置、登出等认证相关业务
 * </p>
 * <p>主要功能包括：</p>
 * <ul>
 *   <li>{@link #login(LoginParam)} - 用户登录，支持多种登录方式和终端类型</li>
 *   <li>{@link #chooseTenant(TenantChoiceParam)} - 选择租户，切换当前登录租户</li>
 *   <li>{@link #secureAdmin()} - 管理员安全校验（已废弃）</li>
 *   <li>{@link #resetPasswordById(Long)} - 根据用户 ID 重置密码为默认密码</li>
 *   <li>{@link #logout()} - 用户登出，清除登录状态和缓存</li>
 *   <li>{@link #updateTenantPreferences(String, List, Long)} - 更新用户租户偏好设置</li>
 * </ul>
 * <p>安全特性：</p>
 * <ul>
 *   <li>支持 SM2 加密传输密码，支持 BCrypt/SM2 密码存储策略</li>
 *   <li>支持登录失败锁定、验证码校验等安全机制</li>
 *   <li>支持配置 {@code security.crypto.transport} 指定 SM2 加密传输策略</li>
 *   <li>支持配置 {@code security.password.policy.store} 指定密码存储方式</li>
 * </ul>
 * <p>依赖说明：</p>
 * <ul>
 *   <li>使用 Sa-Token 框架进行登录认证和 Token 管理</li>
 *   <li>使用 Redis 缓存登录失败次数、锁定状态、在线用户信息等</li>
 *   <li>支持多租户架构，通过 TenantContext 设置当前租户</li>
 *   <li>支持多种登录策略，通过 LoginStrategyFactory 工厂类管理</li>
 *   <li>支持多种验证码策略，通过 CaptchaStrategyFactory 工厂类管理</li>
 *   <li>支持多种租户选择策略，通过 ChooseTenantStrategyFactory 工厂类管理</li>
 *   <li>支持密码加密存储，通过 CryptoPasswordProvider 和 CryptoProviders 管理</li>
 *   <li>支持登录日志记录，通过 LoginLogService 记录登录成功/失败日志</li>
 *   <li>支持 IP 地址定位，通过 IpLocationService 获取登录地区</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.auth.service.AuthService
 * @see com.forgex.common.crypto.CryptoPasswordProvider
 * @see com.forgex.common.domain.config.CryptoTransportConfig
 * @see com.forgex.common.domain.config.PasswordPolicyConfig
 */
@Service
@DS("admin")
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String KEY_SECURITY_PASSWORD_POLICY = "security.password.policy";
    private static final String KEY_SECURITY_LOGIN_FAILURE = "security.login.failure";
    private static final String LOGIN_FAIL_COUNT_KEY_PREFIX = "fx:auth:login:fail:";
    private static final String LOGIN_LOCK_KEY_PREFIX = "fx:auth:login:lock:";
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
    private RedissonClient redissonClient;
    @Autowired
    private LoginLogService loginLogService;
    @Autowired
    private com.forgex.common.util.IpLocationService ipLocationService;
    @Autowired
    private LoginStrategyFactory loginStrategyFactory;
    @Autowired
    private ChooseTenantStrategyFactory chooseTenantStrategyFactory;
    @Autowired
    private CaptchaStrategyFactory captchaStrategyFactory;


    /**
     * 用户登录
     * <p>
     * 处理用户登录请求，支持多种登录方式（账号密码、手机号、邮箱等）和多种终端类型（Web、App 等）
     * </p>
     * <ol>
     *   <li>解析登录终端类型和登录方式，从请求参数中提取并规范化</li>
     *   <li>根据登录终端和登录类型选择对应的登录策略进行处理</li>
     *   <li>登录策略会执行验证码校验、账号密码验证、租户绑定查询等逻辑</li>
     *   <li>返回用户绑定的租户列表，供用户选择要登录的租户</li>
     * </ol>
     * <p>
     * 安全特性：
     * </p>
     * <ul>
     *   <li>支持 SM2 加密传输密码，前端使用公钥加密，后端使用私钥解密</li>
     *   <li>支持 BCrypt 或 SM2 密码存储策略，根据配置自动选择</li>
     *   <li>支持登录失败次数限制和账号锁定机制</li>
     *   <li>支持验证码校验，支持图片验证码和滑块验证码</li>
     * </ul>
     *
     * @param param 登录参数，包含账号、密码、验证码、登录终端、登录类型等信息
     * @return {@link R} 统一响应对象，包含租户列表（TenantVO）
     * @throws I18nBusinessException 当登录失败时抛出业务异常，如账号不存在、密码错误、验证码错误等
     * @see com.forgex.auth.domain.param.LoginParam
     * @see com.forgex.auth.domain.vo.TenantVO
     * @see com.forgex.common.domain.config.CryptoTransportConfig
     * @see com.forgex.common.domain.config.PasswordPolicyConfig
     * @see com.forgex.common.domain.config.CaptchaConfig
     */
    @Override
    public R<List<TenantVO>> login(LoginParam param) {
        String loginTerminal = resolveLoginTerminal(param == null ? null : param.getLoginTerminal());
        String loginType = resolveLoginType(param == null ? null : param.getLoginType());
        if (param != null) {
            param.setLoginTerminal(loginTerminal);
            param.setLoginType(loginType);
        }
        return loginStrategyFactory.getStrategy(loginTerminal, loginType).login(param);
    }

    /**
     * 执行账号密码登录。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    public R<List<TenantVO>> doAccountPasswordLogin(LoginParam param) {
        // 获取账号信息
        String account = param == null ? null : param.getAccount();
        // 获取密码信息
        String password = param == null ? null : param.getPassword();

        // 获取客户端 IP 地址
        String clientIp = getClientIp();
        // 获取 User-Agent 信息
        String userAgent = getUserAgent();
        // 根据 IP 地址获取登录地区信息
        String region = ipLocationService.getLocationByIp(clientIp);

        // 校验账号和密码不能为空
        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            log.warn("Login failed: account or password is empty");
            // 记录登录失败日志
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "account or password is empty");
            return R.fail(CommonPrompt.ACCOUNT_OR_PASSWORD_EMPTY);
        }

        LoginSecurityConfig loginSecurityConfig = getLoginSecurityConfig();
        R<List<TenantVO>> lockResult = checkLoginLocked(account, loginSecurityConfig);
        if (lockResult != null) {
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "account is locked");
            return lockResult;
        }

        // 如果配置了 SM2 加密传输，则解密密码
        CryptoTransportConfig cryptoCfg = configService.getJson("security.crypto.transport", CryptoTransportConfig.class, null);
        if (cryptoCfg != null && StringUtils.hasText(cryptoCfg.getPrivateKey()) && "SM2".equalsIgnoreCase(cryptoCfg.getAlgorithm())) {
            try {
                // 创建 SM2 解密对象
                SM2 sm2 = new SM2(cryptoCfg.getPrivateKey(), cryptoCfg.getPublicKey());
                // 获取密码加密格式配置
                String cipherFmt = cryptoCfg.getCipher();
                // 根据加密格式选择对应的解密方式
                if ("BCD".equalsIgnoreCase(cipherFmt)) {
                    // BCD 编码格式
                    byte[] plain = sm2.decryptFromBcd(password, KeyType.PrivateKey);
                    password = new String(plain, StandardCharsets.UTF_8);
                } else {
                    // 其他格式直接解密为字符串
                    password = sm2.decryptStr(password, KeyType.PrivateKey);
                }
            } catch (Exception ignored) {
                // 解密失败时忽略异常
            }
        }

        // 根据账号查询用户信息
        String idKey = account;
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId, SysUser::getAccount, SysUser::getUsername, SysUser::getPassword, SysUser::getEmail, SysUser::getPhone, SysUser::getStatus)
                .eq(SysUser::getAccount, idKey));
        // 如果用户不存在
        if (user == null) {
            log.warn("用户登录失败：用户不存在 account={}", idKey);
            // 记录登录失败日志
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "user not found");
            recordLoginFailureState(account, loginSecurityConfig);
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }

        // 获取密码策略配置
        PasswordPolicyConfig policy = getPasswordPolicyConfig();
        // 解析密码存储方式
        String store = resolvePasswordStore(policy);
        // 根据存储方式解析对应的密码加密提供者
        CryptoPasswordProvider provider = CryptoProviders.resolve(store, configService);
        // 验证密码
        boolean passOk = provider.verify(password, user.getPassword());
        if (!passOk) {
            log.warn("用户登录失败：密码错误 account={}", account);
            // 记录登录失败日志
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "password incorrect");
            recordLoginFailureState(account, loginSecurityConfig);
            return R.fail(CommonPrompt.PASSWORD_INCORRECT);
        }

        // 验证码校验
        CaptchaConfig cfg = configService.getJson("login.captcha", CaptchaConfig.class, CaptchaConfig.defaults());
        String mode = cfg == null ? null : cfg.getMode();
        CaptchaValidationResult captchaValidationResult = captchaStrategyFactory.getStrategy(mode).validate(param);
        if (!captchaValidationResult.isSuccess()) {
            String logMessage = captchaValidationResult.getLogMessage();
            log.warn("验证码校验失败：{} account={}", logMessage, account);
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, logMessage);
            recordLoginFailureState(account, loginSecurityConfig);
            return R.fail(captchaValidationResult.getPrompt());
        }

        // 清除登录失败状态
        clearLoginFailureState(account);

        // 查询用户绑定的租户列表
        List<SysUserTenant> binds = userTenantMapper.selectList(new LambdaQueryWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, user.getId())
                .orderByDesc(SysUserTenant::getPrefOrder)
                .orderByDesc(SysUserTenant::getLastUsed));
        // 提取租户 ID 列表
        List<Long> tenantIds = new ArrayList<>();
        for (SysUserTenant b : binds) tenantIds.add(b.getTenantId());
        // 如果用户没有绑定任何租户
        if (tenantIds.isEmpty()) {
            log.warn("用户登录成功，但未绑定任何租户 account={}", account);
            return R.ok(Collections.emptyList());
        }
        // 查询租户详细信息
        List<SysTenant> tenants = tenantMapper.selectList(new LambdaQueryWrapper<SysTenant>()
                .in(SysTenant::getId, tenantIds));
        // 构建租户 VO 列表
        List<TenantVO> vos = new ArrayList<>();
        for (SysTenant t : tenants) {
            TenantVO vo = new TenantVO();
            vo.setId(t.getId() == null ? null : String.valueOf(t.getId()));
            vo.setName(t.getTenantName());
            vo.setIntro(t.getDescription());
            vo.setLogo(t.getLogo());
            vo.setTenantType(t.getTenantType() == null ? null : t.getTenantType().getCode());
            // 设置是否为默认租户
            for (SysUserTenant b : binds) { if (b.getTenantId().equals(t.getId())) { vo.setIsDefault(b.getIsDefault()); break; } }
            vos.add(vo);
        }
        log.info("用户登录成功：account={}, tenants={}", idKey, vos.size());
        return R.ok(vos);
    }


    /**
     * 选择租户
     * <p>
     * 用户登录后选择要访问的租户，切换当前会话的租户上下文
     * </p>
     *
     * @param param 租户选择参数，包含租户 ID、账号等信息
     * @return {@link R} 统一响应对象，包含用户 DTO 信息
     * @throws I18nBusinessException 当选择失败时抛出业务异常，如未登录、用户未绑定租户等
     * @see com.forgex.auth.domain.param.TenantChoiceParam
     * @see com.forgex.auth.domain.dto.SysUserDTO
     * @see com.forgex.common.tenant.TenantContext
     * @see cn.dev33.satoken.stp.StpUtil
     */
    @Override
    public R<SysUserDTO> chooseTenant(TenantChoiceParam param) {
        String loginTerminal = resolveLoginTerminal(param == null ? null : param.getLoginTerminal());
        if (param != null) {
            param.setLoginTerminal(loginTerminal);
        }
        return chooseTenantStrategyFactory.getStrategy(loginTerminal).chooseTenant(param);
    }

    /**
     * 选择登录租户。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    public R<SysUserDTO> doChooseTenant(TenantChoiceParam param) {
        // 获取租户 ID
        Long tenantId = param == null ? null : param.getTenantId();
        // 获取账号
        String account = param == null ? null : param.getAccount();
        if (tenantId == null) {
            return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.NOT_LOGIN);
        }
        if (!StringUtils.hasText(account)) return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.ACCOUNT_CANNOT_BE_EMPTY);
        String idKey = account;
        // 查询用户信息
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, idKey));
        if (user == null) return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.USER_NOT_FOUND);
        // 查询用户与租户的绑定关系
        SysUserTenant bind = userTenantMapper.selectOne(new LambdaQueryWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, user.getId())
                .eq(SysUserTenant::getTenantId, tenantId)
                .last("limit 1"));
        if (bind == null) return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.USER_NOT_BOUND_TO_TENANT);
        // 执行登录
        StpUtil.login(idKey);
        // 获取 Token
        String token = StpUtil.getTokenValue();
        populateLoginSession(user.getId(), tenantId, idKey, token);
        // 设置租户上下文
        TenantContext.set(tenantId);
        // 更新用户租户绑定信息（最后使用时间、优先级）
        userTenantMapper.update(null, new LambdaUpdateWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, user.getId())
                .eq(SysUserTenant::getTenantId, tenantId)
                .set(SysUserTenant::getLastUsed, LocalDateTime.now())
                .setSql("pref_order = pref_order + 1"));

        // 获取客户端信息
        String clientIp = getClientIp();
        String userAgent = getUserAgent();
        String region = ipLocationService.getLocationByIp(clientIp);
        // 记录登录成功日志
        loginLogService.recordLoginSuccess(user.getId(), account, tenantId, clientIp, region, userAgent, token);

        // 更新用户最后登录信息
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getLastLoginIp, clientIp)
                .set(SysUser::getLastLoginRegion, region)
                .set(SysUser::getLastLoginTime, LocalDateTime.now()));

        // 缓存在线用户信息到 Redis
        cacheOnlineUserSession(
                user.getId(),
                tenantId,
                idKey,
                token,
                resolveLoginTerminal(param == null ? null : param.getLoginTerminal()),
                clientIp,
                userAgent
        );

        log.info("用户选择租户成功：account={}, tenantId={}", account, tenantId);
        // 构建返回结果
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
     * 更新用户租户偏好设置
     * <p>
     * 允许用户自定义租户列表的显示顺序和默认租户
     * </p>
     *
     * @param account 用户账号
     * @param ordered 租户 ID 有序列表，按优先级排序
     * @param defaultTenantId 默认租户 ID
     * @return {@link R} 统一响应对象
     */
    @Override
    public R<Boolean> updateTenantPreferences(String account, java.util.List<Long> ordered, Long defaultTenantId) {
        if (!StringUtils.hasText(account) || ordered == null) return R.fail(CommonPrompt.BAD_REQUEST);
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getAccount, account));
        if (user == null) return R.fail(CommonPrompt.USER_NOT_FOUND);
        // 遍历租户列表，更新优先级和默认设置
        int n = ordered.size(); int weight = n;
        for (Long tid : ordered) {
            // 更新租户偏好设置
            userTenantMapper.update(null, new LambdaUpdateWrapper<SysUserTenant>()
                    .eq(SysUserTenant::getUserId, user.getId())
                    .eq(SysUserTenant::getTenantId, tid)
                    .set(SysUserTenant::getPrefOrder, weight)
                    .set(SysUserTenant::getIsDefault, (defaultTenantId != null && defaultTenantId.equals(tid))));
            // 权重递减
            weight--;
        }
        return R.ok(true);
    }

    /**
     * 切换登录语言。
     *
     * @param lang 语言
     * @return 统一响应结果
     */
    @Override
    public R<Boolean> changeLanguage(String lang) {
        // 校验语言参数不能为空
        if (!StringUtils.hasText(lang)) {
            return R.fail(500, AuthPromptEnum.LANG_EMPTY);
        }
        // 获取当前用户 ID
        Long userId = CurrentUserUtils.getUserId();
        // 获取当前租户 ID
        Long tenantId = CurrentUserUtils.getTenantId();
        // 校验用户必须已登录
        if (userId == null || tenantId == null) {
            return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.NOT_LOGIN);
        }

        // 构建缓存键
        String prefKey = "fx:lang:" + tenantId + ":" + userId;
        try {
            // 保存语言设置到 Redis
            redis.opsForValue().set(prefKey, lang);
        } catch (Exception e) {
            // 保存失败返回错误
            return R.fail(500, AuthPromptEnum.LANG_SET_FAILED);
        }

        return R.ok(true);
    }


    /**
     * 管理员安全校验（已废弃）
     *
     * @return {@link R} 统一响应对象
     * @see cn.dev33.satoken.stp.StpUtil#checkRole(String)
     */
    @Override
    @Deprecated
    public R<Boolean> secureAdmin() {
        // 检查是否已登录
        StpUtil.checkLogin();
        return R.fail(CommonPrompt.BAD_REQUEST);
    }

    /**
     * 重置用户密码
     * <p>
     * 根据用户 ID 重置密码为默认密码（123456），使用 BCrypt 或 SM2 加密存储
     * </p>
     *
     * @param userId 用户 ID
     * @return {@link R} 统一响应对象
     */
    @Override
    public R<Boolean> resetPasswordById(Long userId) {
        if (userId == null) {
            return R.fail(CommonPrompt.USER_ID_CANNOT_BE_EMPTY);
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }
        String hashed = encryptPassword(resolveDefaultPassword());
        int n = userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getPassword, hashed)
                .eq(SysUser::getId, userId));
        if (n > 0) {
            return R.ok(true);
        }
        return R.fail(CommonPrompt.RESET_FAILED);
    }

    /**
     * 解析登录终端类型
     * <p>
     * 从配置中读取登录终端类型，支持 Web、App 等多种终端
     * </p>
     *
     * @param loginTerminal 登录终端参数
     * @return 规范化后的登录终端类型
     * @see com.forgex.common.domain.config.PasswordPolicyConfig
     * @see com.forgex.common.config.ConfigService#getJson(String, Class, Object)
     */
    private String resolveLoginTerminal(String loginTerminal) {
        return StringUtils.hasText(loginTerminal) ? loginTerminal.trim().toUpperCase(Locale.ROOT) : AuthTerminalConstants.B;
    }

    private String resolveLoginType(String loginType) {
        return StringUtils.hasText(loginType) ? loginType.trim().toUpperCase(Locale.ROOT) : LoginTypeConstants.ACCOUNT_PASSWORD;
    }

    private PasswordPolicyConfig getPasswordPolicyConfig() {
        PasswordPolicyConfig defaults = new PasswordPolicyConfig();
        defaults.setStore("bcrypt");
        defaults.setDefaultPassword("Aa123456");
        PasswordPolicyConfig policy = configService.getJson(KEY_SECURITY_PASSWORD_POLICY, PasswordPolicyConfig.class, defaults);
        return policy == null ? defaults : policy;
    }

    /**
     * 解析密码存储方式
     * <p>
     * 从密码策略配置中解析密码存储方式，支持 bcrypt、sm2 等
     * </p>
     *
     * @param policy 密码策略配置
     * @return 密码存储方式
     * @see com.forgex.common.domain.config.PasswordPolicyConfig#getStore()
     */
    private String resolvePasswordStore(PasswordPolicyConfig policy) {
        return policy == null || !StringUtils.hasText(policy.getStore()) ? "bcrypt" : policy.getStore();
    }

    /**
     * 解析默认密码
     * <p>
     * 从密码策略配置中读取默认密码，用于密码重置
     * </p>
     *
     * @return 默认密码
     * @see com.forgex.common.domain.config.PasswordPolicyConfig#getDefaultPassword()
     */
    private String resolveDefaultPassword() {
        PasswordPolicyConfig policy = getPasswordPolicyConfig();
        return StringUtils.hasText(policy.getDefaultPassword()) ? policy.getDefaultPassword() : "Aa123456";
    }

    /**
     * 加密密码
     * <p>
     * 根据密码存储策略对原始密码进行加密或哈希处理
     * </p>
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     * @see com.forgex.common.crypto.CryptoPasswordProvider#encrypt(String)
     * @see com.forgex.common.crypto.CryptoProviders#resolve(String, com.forgex.common.config.ConfigService)
     */
    private String encryptPassword(String rawPassword) {
        CryptoPasswordProvider provider = CryptoProviders.resolve(resolvePasswordStore(getPasswordPolicyConfig()), configService);
        if (provider.supportsEncrypt()) {
            return provider.encrypt(rawPassword);
        }
        if (provider.supportsHash()) {
            return provider.hash(rawPassword);
        }
        throw new IllegalStateException("Unsupported password store: " + provider.name());
    }

    /**
     * 获取登录安全配置
     * <p>
     * 从配置中读取登录安全相关配置，包括失败次数限制、锁定时间等
     * </p>
     *
     * @return 登录安全配置
     * @see com.forgex.common.domain.config.LoginSecurityConfig
     * @see com.forgex.common.config.ConfigService#getJson(String, Class, Object)
     */
    private LoginSecurityConfig getLoginSecurityConfig() {
        LoginSecurityConfig defaults = LoginSecurityConfig.defaults();
        LoginSecurityConfig config = configService.getJson(KEY_SECURITY_LOGIN_FAILURE, LoginSecurityConfig.class, defaults);
        return config == null ? defaults : config;
    }

    /**
     * 检查账号是否被锁定
     * <p>
     * 根据登录失败配置检查账号是否被锁定，如果锁定则返回锁定剩余时间
     * </p>
     *
     * @param account 账号
     * @param config 登录安全配置
     * @return 如果账号被锁定返回错误响应，否则返回 null
     * @see com.forgex.common.domain.config.LoginSecurityConfig#getLockMinutes()
     */
    private R<List<TenantVO>> checkLoginLocked(String account, LoginSecurityConfig config) {
        if (!StringUtils.hasText(account) || config == null || config.getLockMinutes() == null || config.getLockMinutes() <= 0) {
            return null;
        }
        String lockKey = LOGIN_LOCK_KEY_PREFIX + normalizeAccountKey(account);
        Long ttl = readKeyTtlSeconds(lockKey);
        if (ttl == null || ttl <= 0) {
            return null;
        }
        long minutes = Math.max(1L, (ttl + 59) / 60);
        return R.fail(CommonPrompt.ACCOUNT_LOCKED, String.valueOf(minutes));
    }

    /**
     * 记录登录失败状态
     * <p>
     * 记录登录失败次数，如果达到最大失败次数则锁定账号
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>从配置中读取失败时间窗口、最大失败次数、锁定时间等参数</li>
     *   <li>使用 Redisson 原子计数器记录失败次数</li>
     *   <li>如果失败次数达到阈值，设置账号锁定并清除失败计数器</li>
     * </ol>
     *
     * @param account 账号
     * @param config 登录安全配置
     * @see com.forgex.common.domain.config.LoginSecurityConfig#getMaxFailCount()
     * @see com.forgex.common.domain.config.LoginSecurityConfig#getLockMinutes()
     */
    private void recordLoginFailureState(String account, LoginSecurityConfig config) {
        if (!StringUtils.hasText(account) || config == null) {
            return;
        }
        Integer failWindowMinutes = config.getFailWindowMinutes();
        Integer maxFailCount = config.getMaxFailCount();
        Integer lockMinutes = config.getLockMinutes();
        if (failWindowMinutes == null || failWindowMinutes <= 0 || maxFailCount == null || maxFailCount <= 0) {
            return;
        }
        String accountKey = normalizeAccountKey(account);
        String failKey = LOGIN_FAIL_COUNT_KEY_PREFIX + accountKey;
        try {
            RAtomicLong failCounter = redissonClient.getAtomicLong(failKey);
            long currentCount = failCounter.incrementAndGet();
            failCounter.expire(Duration.ofMinutes(failWindowMinutes));
            if (currentCount >= maxFailCount && lockMinutes != null && lockMinutes > 0) {
                redissonClient.getBucket(LOGIN_LOCK_KEY_PREFIX + accountKey).set("1", Duration.ofMinutes(lockMinutes));
                failCounter.delete();
            }
        } catch (Exception e) {
            log.warn("记录登录失败状态失败：account={}", account, e);
        }
    }

    /**
     * 清除登录失败状态
     * <p>
     * 登录成功后清除登录失败计数器和账号锁定状态
     * </p>
     *
     * @param account 账号
     */
    private void clearLoginFailureState(String account) {
        if (!StringUtils.hasText(account)) {
            return;
        }
        String accountKey = normalizeAccountKey(account);
        try {
            redissonClient.getAtomicLong(LOGIN_FAIL_COUNT_KEY_PREFIX + accountKey).delete();
            redissonClient.getBucket(LOGIN_LOCK_KEY_PREFIX + accountKey).delete();
        } catch (Exception e) {
            log.warn("清除登录失败状态失败：account={}", account, e);
        }
    }

    /**
     * 规范化账号键
     * <p>
     * 将账号转换为小写并去除首尾空格，用于 Redis 键名
     * </p>
     *
     * @param account 账号
     * @return 规范化后的账号键
     */
    private String normalizeAccountKey(String account) {
        return account == null ? "" : account.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 读取 Redis 键的剩余生存时间
     * <p>
     * 使用 Redisson 读取键的 TTL，返回秒数
     * </p>
     *
     * @param key Redis 键
     * @return 剩余生存时间（秒），如果键不存在或已过期返回 null
     */
    private Long readKeyTtlSeconds(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        try {
            RBucket<Object> bucket = redissonClient.getBucket(key);
            long ttlMillis = bucket.remainTimeToLive();
            if (ttlMillis <= 0L) {
                return null;
            }
            return Math.max(1L, (ttlMillis + 999L) / 1000L);
        } catch (Exception e) {
            log.warn("读取 Redis 键 TTL 失败：key={}", key, e);
            return null;
        }
    }

    /**
     * Map 转 JSON 字符串
     * <p>
     * 使用 Hutool 工具类将 Map 对象转换为 JSON 字符串
     * </p>
     *
     * @param value Map 对象
     * @return JSON 字符串
     * @see cn.hutool.json.JSONUtil#toJsonStr(Object)
     */
    private String toJson(Map<String, Object> value) {
        return JSONUtil.toJsonStr(value);
    }

    /**
     * 构建并写入登录会话上下文
     * <p>
     * 1. 当前 token 对应的 token-session 写入租户级上下文，避免多端登录互相覆盖。
     * 2. 同步兼容旧的 login-session，保证仍依赖旧会话读取逻辑的代码可以继续工作。
     * </p>
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param account 登录账号
     * @param token 当前 token
     */
    private void populateLoginSession(Long userId, Long tenantId, String account, String token) {
        // 1. 写入 token 维度会话，供多端并行登录场景优先读取。
        writeLoginSession(LoginSessionSupport.getCurrentTokenSession(), userId, tenantId, account);

        // 2. 同步写入旧会话，兼容仍通过 loginId 会话读取上下文的旧代码。
        try {
            writeLoginSession(StpUtil.getSession(), userId, tenantId, account);
        } catch (Exception e) {
            log.warn("写入兼容登录会话失败：account={}, token={}", account, token, e);
        }
    }

    /**
     * 写入统一的登录会话字段
     *
     * @param session Sa-Token 会话
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param account 登录账号
     */
    private void writeLoginSession(SaSession session, Long userId, Long tenantId, String account) {
        if (session == null) {
            return;
        }
        session.set(LoginSessionKeys.KEY_USER_ID, userId);
        session.set(LoginSessionKeys.KEY_TENANT_ID, tenantId);
        session.set(LoginSessionKeys.KEY_ACCOUNT, account);
    }

    /**
     * 构建 token 维度在线用户缓存 key
     *
     * @param tenantId 租户 ID
     * @param userId 用户 ID
     * @param token token 值
     * @return 在线用户缓存 key
     */
    private String buildOnlineUserKey(Long tenantId, Long userId, String token) {
        return "fx:online:user:" + tenantId + ":" + userId + ":" + token;
    }

    /**
     * 构建旧版在线用户缓存 key
     *
     * @param tenantId 租户 ID
     * @param userId 用户 ID
     * @return 旧版在线用户缓存 key
     */
    private String buildLegacyOnlineUserKey(Long tenantId, Long userId) {
        return "fx:online:user:" + tenantId + ":" + userId;
    }

    /**
     * 缓存在线用户会话
     * <p>
     * 1. 以 token 维度写入在线记录，支持同账号多端并行展示。
     * 2. TTL 与当前 token 有效期保持一致。
     * 3. 写入新 key 后顺带删除旧的按用户维度缓存，避免新旧展示重复。
     * </p>
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param account 登录账号
     * @param token token 值
     * @param loginTerminal 登录终端
     * @param clientIp 客户端 IP
     * @param userAgent User-Agent
     */
    private void cacheOnlineUserSession(Long userId,
                                        Long tenantId,
                                        String account,
                                        String token,
                                        String loginTerminal,
                                        String clientIp,
                                        String userAgent) {
        if (userId == null || tenantId == null || !StringUtils.hasText(token)) {
            return;
        }
        String onlineKey = buildOnlineUserKey(tenantId, userId, token);
        String legacyKey = buildLegacyOnlineUserKey(tenantId, userId);

        Map<String, Object> onlineUser = new HashMap<>();
        onlineUser.put("userId", userId);
        onlineUser.put("tenantId", tenantId);
        onlineUser.put("account", account);
        onlineUser.put("token", token);
        onlineUser.put("loginTerminal", resolveLoginTerminal(loginTerminal));
        onlineUser.put("loginTime", LocalDateTime.now().toString());
        onlineUser.put("clientIp", clientIp);
        onlineUser.put("userAgent", userAgent);

        String json = toJson(onlineUser);
        Duration ttl = resolveCurrentTokenTtl(token);
        try {
            if (ttl == null) {
                redis.opsForValue().set(onlineKey, json);
            } else if (!ttl.isZero() && !ttl.isNegative()) {
                redis.opsForValue().set(onlineKey, json, ttl);
            } else {
                redis.delete(onlineKey);
            }
            redis.delete(legacyKey);
        } catch (Exception e) {
            log.warn("缓存在线用户会话失败：account={}, tenantId={}, token={}", account, tenantId, token, e);
        }
    }

    /**
     * 解析当前 Token 的 TTL
     * <p>
     * 根据 Token 解析其有效的生存时间
     * </p>
     *
     * @param token Token 字符串
     * @return Token 的生存时间，如果无效返回 null
     * @see #resolveEffectiveTtlSeconds(String)
     */
    private Duration resolveCurrentTokenTtl(String token) {
        Long ttlSeconds = resolveEffectiveTtlSeconds(token);
        return ttlSeconds == null ? null : Duration.ofSeconds(ttlSeconds);
    }

    /**
     * 解析有效的超时时间
     * <p>
     * 从 Token 中解析 tokenTimeout 和 activeTimeout，返回较小的值作为有效超时时间
     * </p>
     *
     * @param token Token 字符串
     * @return 有效的超时时间（秒），如果无效返回 null
     * @see #readTokenTimeout(String)
     * @see #readActiveTimeout(String)
     * @see #normalizeTimeout(long)
     */
    private Long resolveEffectiveTtlSeconds(String token) {
        if (!StringUtils.hasText(token)) {
            return 0L;
        }
        Long tokenTimeout = normalizeTimeout(readTokenTimeout(token));
        Long activeTimeout = normalizeTimeout(readActiveTimeout(token));
        if (tokenTimeout == null) {
            return activeTimeout;
        }
        if (activeTimeout == null) {
            return tokenTimeout;
        }
        return Math.min(tokenTimeout, activeTimeout);
    }

    /**
     * 读取 Token 超时时间
     * <p>
     * 从 Sa-Token 中读取 Token 的超时时间
     * </p>
     *
     * @param token Token 字符串
     * @return Token 超时时间（秒），如果读取失败返回 0
     * @see cn.dev33.satoken.stp.StpUtil#getTokenTimeout(String)
     */
    private long readTokenTimeout(String token) {
        try {
            return StpUtil.getTokenTimeout(token);
        } catch (Exception ignored) {
            return 0L;
        }
    }

    /**
     * 读取 Token 活跃超时时间
     * <p>
     * 从 Sa-Token 中读取 Token 的活跃超时时间
     * </p>
     *
     * @param token Token 字符串
     * @return Token 活跃超时时间（秒），如果读取失败返回 NOT_VALUE_EXPIRE
     * @see cn.dev33.satoken.dao.SaTokenDao#NOT_VALUE_EXPIRE
     */
    private long readActiveTimeout(String token) {
        try {
            return StpUtil.getStpLogic().getTokenActiveTimeoutByToken(token);
        } catch (Exception ignored) {
            return SaTokenDao.NOT_VALUE_EXPIRE;
        }
    }

    /**
     * 规范化超时时间
     * <p>
     * 处理 Sa-Token 的特殊超时时间值（NEVER_EXPIRE、NOT_VALUE_EXPIRE）
     * </p>
     *
     * @param seconds 超时时间（秒）
     * @return 规范化后的超时时间，永不过期或无效返回 null
     * @see cn.dev33.satoken.dao.SaTokenDao#NEVER_EXPIRE
     * @see cn.dev33.satoken.dao.SaTokenDao#NOT_VALUE_EXPIRE
     */
    private Long normalizeTimeout(long seconds) {
        if (seconds == SaTokenDao.NEVER_EXPIRE || seconds == SaTokenDao.NOT_VALUE_EXPIRE) {
            return null;
        }
        if (seconds <= 0) {
            return 0L;
        }
        return seconds;
    }

    /**
     * 清除在线用户缓存
     * <p>
     * 根据 Token 或租户 ID+ 用户 ID 清除 Redis 中的在线用户缓存
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>如果提供了租户 ID 和用户 ID，直接删除对应的在线缓存</li>
     *   <li>否则遍历所有在线用户缓存，查找匹配的 Token 并删除</li>
     * </ol>
     *
     * @param tokenValue Token 值
     * @param tenantId 租户 ID
     * @param userId 用户 ID
     */
    private void clearOnlineUserCache(String tokenValue, Long tenantId, Long userId) {
        if (StringUtils.hasText(tokenValue) && tenantId != null && userId != null) {
            try {
                redis.delete(buildOnlineUserKey(tenantId, userId, tokenValue));
                log.info("清除在线用户缓存：userId={}, tenantId={}, token={}", userId, tenantId, tokenValue);
            } catch (Exception e) {
                log.warn("清除在线用户缓存失败：{}", e.getMessage());
            }
        }
        if (tenantId != null && userId != null) {
            try {
                redis.delete(buildLegacyOnlineUserKey(tenantId, userId));
            } catch (Exception ignored) {
            }
        }
        if (!StringUtils.hasText(tokenValue)) {
            return;
        }
        try {
            Set<String> keys = redis.keys("fx:online:user:*");
            if (keys == null || keys.isEmpty()) {
                return;
            }
            for (String key : keys) {
                String onlineJson = redis.opsForValue().get(key);
                if (!StringUtils.hasText(onlineJson)) {
                    continue;
                }
                try {
                    cn.hutool.json.JSONObject onlineObj = JSONUtil.parseObj(onlineJson);
                    if (tokenValue.equals(onlineObj.getStr("token"))) {
                        redis.delete(key);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            log.warn("清除在线用户缓存失败：{}", e.getMessage());
        }
    }

    /**
     * 用户登出
     * <p>
     * 处理用户登出请求，清除登录状态和在线缓存
     * </p>
     *
     * @return {@link R} 统一响应对象
     * @see StpUtil#logout()
     */
    @Override
    public R<Boolean> logout() {
        try {
            // 获取当前登录用户 ID
            Object uid = StpUtil.getLoginIdDefaultNull();

            // 初始化用户信息变量
            Long userId = null;
            Long tenantId = null;
            String account = null;
            String tokenValue = null;

            // 获取 Token 值
            try {
                tokenValue = StpUtil.getTokenValue();
            } catch (Exception e) {
                log.debug("获取 Token 失败：{}", e.getMessage());
            }

            // 从 Session 中获取用户信息
            try {
                SaSession session = LoginSessionSupport.getCurrentSession();
                // 从 Session 中读取用户 ID、租户 ID 等信息
                if (session != null) {
                    // 获取用户 ID
                    Object uidObj = session.get(LoginSessionKeys.KEY_USER_ID);
                    if (uidObj instanceof Long) {
                        userId = (Long) uidObj;
                    } else if (uidObj instanceof Integer) {
                        userId = ((Integer) uidObj).longValue();
                    } else if (uidObj instanceof String) {
                        try { userId = Long.valueOf((String) uidObj); } catch (Exception ignored) { }
                    }
                    // 获取租户 ID
                    Object tidObj = session.get(LoginSessionKeys.KEY_TENANT_ID);
                    if (tidObj instanceof Long) {
                        tenantId = (Long) tidObj;
                    } else if (tidObj instanceof Integer) {
                        tenantId = ((Integer) tidObj).longValue();
                    } else if (tidObj instanceof String) {
                        try { tenantId = Long.valueOf((String) tidObj); } catch (Exception ignored) { }
                    }
                }
            } catch (Exception e) {
                log.debug("获取 Session 失败：{}", e.getMessage());
            }

            // 获取账号
            account = uid == null ? null : String.valueOf(uid);

            // 记录登出日志
            if (StringUtils.hasText(tokenValue)) {
                try {
                    loginLogService.recordLogoutByToken(tokenValue, com.forgex.common.security.LogoutReason.MANUAL);
                } catch (Exception e) {
                    log.warn("记录登出日志失败：{}", e.getMessage());
                }
            }

            // 清除在线用户缓存
            clearOnlineUserCache(tokenValue, tenantId, userId);

            // 执行登出
            try {
                StpUtil.logout();
            } catch (Exception e) {
                log.debug("Sa-Token 登出失败：{}", e.getMessage());
            }

            log.info("用户登出成功：account={}", account);
            return R.ok(true);
        } catch (Exception e) {
            // 登出失败记录错误日志
            log.error("Logout process failed", e);
            return R.fail(CommonPrompt.LOGOUT_FAILED);
        }
    }

    /**
     * 获取客户端 IP 地址
     * <p>
     * 从 HTTP 请求头中获取客户端 IP 地址，优先使用 X-Client-IP 头
     * </p>
     *
     * @return 客户端 IP 地址，如果获取失败返回"unknown"
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
            log.warn("Get client IP failed", e);
        }
        return "unknown";
    }

    /**
     * 获取 User-Agent
     * <p>
     * 从 HTTP 请求头中获取 User-Agent 信息
     * </p>
     *
     * @return User-Agent 字符串，如果获取失败返回"unknown"
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
            log.warn("Get User-Agent failed", e);
        }
        return "unknown";
    }

    // ==================== 邀请码相关功能 ====================

    @Autowired
    private SysInviteCodeMapper inviteCodeMapper;

    @Autowired
    private SysInviteRegisterRecordMapper inviteRegisterRecordMapper;

    @Autowired
    private SysUserRolePermMapper userRolePermMapper;

    @Autowired
    private SysRolePermMapper rolePermMapper;

    /**
     * 用户注册
     * <p>
     * 处理用户注册请求，支持邀请码注册
     * </p>
     */
    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public R<Boolean> register(com.forgex.auth.domain.param.RegisterParam param) {
        boolean oldIgnore = TenantContextIgnore.isIgnore();
        TenantContextIgnore.setIgnore(true);
        try {
            return doRegister(param);
        } finally {
            restoreTenantIgnore(oldIgnore);
        }
    }

    private R<Boolean> doRegister(com.forgex.auth.domain.param.RegisterParam param) {
        // 1. 校验请求参数
        if (param == null) {
            return R.fail(CommonPrompt.BAD_REQUEST);
        }
        String account = param.getAccount();
        String username = param.getUsername();
        String password = param.getPassword();
        String inviteCode = param.getInviteCode();

        if (!StringUtils.hasText(account)) {
            return R.fail(CommonPrompt.ACCOUNT_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(password)) {
            return R.fail(CommonPrompt.PASSWORD_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(inviteCode)) {
            return R.fail(CommonPrompt.INVITE_CODE_CANNOT_BE_EMPTY);
        }

        // 2. 验证码校验
        CaptchaConfig cfg = configService.getJson("login.captcha", CaptchaConfig.class, CaptchaConfig.defaults());
        String mode = cfg.getMode();
        if ("image".equalsIgnoreCase(mode)) {
            if (!StringUtils.hasText(param.getCaptchaId()) || !StringUtils.hasText(param.getCaptcha())) {
                return R.fail(CommonPrompt.VERIFICATION_CODE_CANNOT_BE_EMPTY);
            }
            if (!captchaService.verifyImage(param.getCaptchaId(), param.getCaptcha())) {
                return R.fail(CommonPrompt.VERIFICATION_CODE_INCORRECT);
            }
        } else if ("slider".equalsIgnoreCase(mode)) {
            if (!StringUtils.hasText(param.getCaptcha())) {
                return R.fail(CommonPrompt.VERIFICATION_CODE_CANNOT_BE_EMPTY);
            }
            if (!captchaService.verifySlider(param.getCaptcha())) {
                return R.fail(CommonPrompt.VERIFICATION_CODE_INCORRECT);
            }
        }

        // 3. 校验邀请码
        com.forgex.auth.domain.entity.SysInviteCode inviteCodeEntity = inviteCodeMapper.selectOne(
                new LambdaQueryWrapper<com.forgex.auth.domain.entity.SysInviteCode>()
                        .eq(com.forgex.auth.domain.entity.SysInviteCode::getInviteCode, inviteCode)
                        .eq(com.forgex.auth.domain.entity.SysInviteCode::getDeleted, false));
        if (inviteCodeEntity == null) {
            return R.fail(CommonPrompt.INVITE_CODE_NOT_FOUND);
        }
        if (Boolean.FALSE.equals(inviteCodeEntity.getStatus())) {
            return R.fail(CommonPrompt.INVITE_CODE_DISABLED);
        }
        if (inviteCodeEntity.getExpireTime() != null && LocalDateTime.now().isAfter(inviteCodeEntity.getExpireTime())) {
            return R.fail(CommonPrompt.INVITE_CODE_EXPIRED);
        }
        if (inviteCodeEntity.getUsedCount() >= inviteCodeEntity.getMaxRegisterCount()) {
            return R.fail(CommonPrompt.INVITE_CODE_USED_UP);
        }
        Long inviteTenantId = inviteCodeEntity.getTenantId();
        Long roleId = inviteCodeEntity.getRoleId();
        if (!isValidInviteRole(roleId, inviteTenantId)) {
            return R.fail(AuthPromptEnum.ROLE_NOT_FOUND);
        }

        // 4. 校验账号是否已存在
        Long existCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, account));
        if (existCount > 0) {
            return R.fail(CommonPrompt.ACCOUNT_ALREADY_EXISTS);
        }

        // 5. 如果配置了 SM2 加密传输，则解密密码
        String rawPassword = password;
        CryptoTransportConfig cryptoCfg = configService.getJson("security.crypto.transport", CryptoTransportConfig.class, null);
        if (cryptoCfg != null && StringUtils.hasText(cryptoCfg.getPrivateKey()) && "SM2".equalsIgnoreCase(cryptoCfg.getAlgorithm())) {
            try {
                SM2 sm2 = new SM2(cryptoCfg.getPrivateKey(), cryptoCfg.getPublicKey());
                String cipherFmt = cryptoCfg.getCipher();
                if ("BCD".equalsIgnoreCase(cipherFmt)) {
                    byte[] plain = sm2.decryptFromBcd(password, KeyType.PrivateKey);
                    rawPassword = new String(plain, StandardCharsets.UTF_8);
                } else {
                    rawPassword = sm2.decryptStr(password, KeyType.PrivateKey);
                }
            } catch (Exception ignored) {
                // 解密失败时忽略异常
            }
        }

        // 6. 加密密码
        String hashedPassword = encryptPassword(rawPassword);

        // 7. 创建用户
        SysUser newUser = new SysUser();
        newUser.setAccount(account);
        newUser.setUsername(StringUtils.hasText(username) ? username : account);
        newUser.setPassword(hashedPassword);
        newUser.setPhone(param.getPhone());
        newUser.setEmail(param.getEmail());
        newUser.setStatus(true);
        newUser.setUserSource(UserSourceEnum.SELF_REGISTERED.getCode());
        userMapper.insert(newUser);

        Long userId = newUser.getId();

        // 8. 如果邀请码配置了部门 ID，则更新用户部门和职位
        if (inviteCodeEntity.getDepartmentId() != null) {
            LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SysUser::getId, userId);
            // 使用 setSql 直接设置部门和职位
            StringBuilder sqlParts = new StringBuilder();
            sqlParts.append("department_id = ").append(inviteCodeEntity.getDepartmentId());
            if (inviteCodeEntity.getPositionId() != null) {
                sqlParts.append(", position_id = ").append(inviteCodeEntity.getPositionId());
            }
            updateWrapper.setSql(sqlParts.toString());
            userMapper.update(null, updateWrapper);
        }

        // 9. 创建用户与租户的绑定关系
        if (inviteTenantId != null) {
            SysUserTenant userTenant = new SysUserTenant();
            userTenant.setUserId(userId);
            userTenant.setTenantId(inviteTenantId);
            userTenant.setIsDefault(true);
            userTenant.setPrefOrder(1);
            userTenant.setLastUsed(LocalDateTime.now());
            userTenantMapper.insert(userTenant);
        }

        // 10. 创建用户与邀请码绑定角色的关系
        SysUserRolePerm userRole = new SysUserRolePerm();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setTenantId(inviteTenantId);
        userRolePermMapper.insert(userRole);

        // 11. 更新邀请码使用次数
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.forgex.auth.domain.entity.SysInviteCode> inviteUpdate =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        inviteUpdate.eq(com.forgex.auth.domain.entity.SysInviteCode::getId, inviteCodeEntity.getId())
                .setSql("used_count = used_count + 1");
        inviteCodeMapper.update(null, inviteUpdate);

        // 12. 记录邀请注册日志
        String clientIp = getClientIp();
        String region = ipLocationService.getLocationByIp(clientIp);

        com.forgex.auth.domain.entity.SysInviteRegisterRecord record = new com.forgex.auth.domain.entity.SysInviteRegisterRecord();
        record.setTenantId(inviteTenantId);
        record.setInviteId(inviteCodeEntity.getId());
        record.setInviteCode(inviteCode);
        record.setUserId(userId);
        record.setAccount(account);
        record.setUsername(StringUtils.hasText(username) ? username : account);
        record.setDepartmentId(inviteCodeEntity.getDepartmentId());
        record.setPositionId(inviteCodeEntity.getPositionId());
        record.setRoleId(roleId);
        record.setRegisterIp(clientIp);
        record.setRegisterRegion(region);
        record.setRegisterTime(LocalDateTime.now());
        record.setStatus(1);
        inviteRegisterRecordMapper.insert(record);

        log.info("用户注册成功：account={}, inviteCode={}, inviteTenantId={}", account, inviteCode, inviteTenantId);

        return R.ok(CommonPrompt.REGISTER_SUCCESS, true);
    }

    /**
     * 校验邀请码
     */
    @Override
    public R<Boolean> validateInviteCode(String inviteCode) {
        boolean oldIgnore = TenantContextIgnore.isIgnore();
        TenantContextIgnore.setIgnore(true);
        try {
            return doValidateInviteCode(inviteCode);
        } finally {
            restoreTenantIgnore(oldIgnore);
        }
    }

    private R<Boolean> doValidateInviteCode(String inviteCode) {
        if (!StringUtils.hasText(inviteCode)) {
            return R.fail(CommonPrompt.INVITE_CODE_CANNOT_BE_EMPTY);
        }
        com.forgex.auth.domain.entity.SysInviteCode entity = inviteCodeMapper.selectOne(
                new LambdaQueryWrapper<com.forgex.auth.domain.entity.SysInviteCode>()
                        .eq(com.forgex.auth.domain.entity.SysInviteCode::getInviteCode, inviteCode)
                        .eq(com.forgex.auth.domain.entity.SysInviteCode::getDeleted, false));
        if (entity == null) {
            return R.fail(CommonPrompt.INVITE_CODE_NOT_FOUND);
        }
        if (Boolean.FALSE.equals(entity.getStatus())) {
            return R.fail(CommonPrompt.INVITE_CODE_DISABLED);
        }
        if (entity.getExpireTime() != null && LocalDateTime.now().isAfter(entity.getExpireTime())) {
            return R.fail(CommonPrompt.INVITE_CODE_EXPIRED);
        }
        if (entity.getUsedCount() >= entity.getMaxRegisterCount()) {
            return R.fail(CommonPrompt.INVITE_CODE_USED_UP);
        }
        if (!isValidInviteRole(entity.getRoleId(), entity.getTenantId())) {
            return R.fail(AuthPromptEnum.ROLE_NOT_FOUND);
        }
        return R.ok(true);
    }

    private void restoreTenantIgnore(boolean oldIgnore) {
        if (oldIgnore) {
            TenantContextIgnore.setIgnore(true);
        } else {
            TenantContextIgnore.clear();
        }
    }

    private boolean isValidInviteRole(Long roleId, Long inviteTenantId) {
        if (roleId == null || inviteTenantId == null) {
            return false;
        }
        SysRolePerm role = rolePermMapper.selectOne(new LambdaQueryWrapper<SysRolePerm>()
                .eq(SysRolePerm::getId, roleId)
                .eq(SysRolePerm::getTenantId, inviteTenantId)
                .eq(SysRolePerm::getStatus, true)
                .eq(SysRolePerm::getDeleted, false));
        return role != null;
    }
}

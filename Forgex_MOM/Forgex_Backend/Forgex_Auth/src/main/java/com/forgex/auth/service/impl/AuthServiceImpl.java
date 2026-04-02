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
import com.forgex.auth.service.LoginLogService;
import com.forgex.common.config.ConfigService;
import com.forgex.auth.service.CaptchaService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
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
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.auth.enums.AuthPromptEnum;
import com.forgex.common.crypto.CryptoProviders;

/**
 * 认证服务实现。
 * <p>
 * 负责登录流程与租户选择、验证码校验、密码校验等逻辑，数据源为 admin 库。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #login(LoginParam)} - 用户登录，返回绑定的租户列表</li>
 *   <li>{@link #chooseTenant(TenantChoiceParam)} - 选择租户，设置当前租户上下文</li>
 *   <li>{@link #secureAdmin()} - 管理员权限校验</li>
 *   <li>{@link #resetPasswordById(Long)} - 重置用户密码</li>
 *   <li>{@link #logout()} - 用户登出</li>
 *   <li>{@link #updateTenantPreferences(String, List, Long)} - 更新租户偏好设置</li>
 * </ul>
 * <p>使用：</p>
 * <ul>
 *   <li>{@link #login(LoginParam)} 登录并返回可选租户列表；</li>
 *   <li>{@link #chooseTenant(TenantChoiceParam)} 选择默认租户；</li>
 *   <li>按配置读取 {@code security.crypto.transport} 执行 SM2 传输解密，再按 {@code security.password.policy.store} 进行存储校验。</li>
 * </ul>
 * <p>可扩展性：</p>
 * <ul>
 *   <li>支持在配置库扩展验证码模式与密码策略；</li>
 *   <li>新增密码算法时在 common 模块的 Provider 中扩展。</li>
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
     * 完整登录流程：
     * </p>
     * <ol>
     *   <li>参数校验：检查账号和密码是否为空</li>
     *   <li>获取客户端信息：IP 地址、User-Agent、地理位置</li>
     *   <li>密码解密：如果启用了 SM2 传输加密，解密密码</li>
     *   <li>用户查询：根据账号查询用户信息</li>
     *   <li>密码验证：根据密码策略（bcrypt/SM2）验证密码</li>
     *   <li>验证码校验：根据配置校验图片验证码或滑块验证码</li>
     *   <li>租户查询：查询用户绑定的所有租户</li>
     *   <li>返回租户列表：将租户实体转换为 VO 返回</li>
     * </ol>
     * <p>
     * 安全特性：
     * </p>
     * <ul>
     *   <li>支持 SM2 国密算法加密传输密码</li>
     *   <li>支持 bcrypt 或 SM2 加密存储密码</li>
     *   <li>支持图片验证码和滑块验证码两种模式</li>
     *   <li>记录登录成功/失败日志</li>
     * </ul>
     *
     * @param param 登录参数，包含账号、密码、验证码等信息
     * @return {@link R} 包含租户 VO 列表的统一返回结构
     * @throws BusinessException 当参数校验失败、用户不存在、密码错误、验证码错误时抛出
     * @see com.forgex.auth.domain.param.LoginParam
     * @see com.forgex.auth.domain.vo.TenantVO
     * @see com.forgex.common.domain.config.CryptoTransportConfig
     * @see com.forgex.common.domain.config.PasswordPolicyConfig
     * @see com.forgex.common.domain.config.CaptchaConfig
     */
    @Override
    public R<List<TenantVO>> login(LoginParam param) {
        // 从参数中提取账号
        String account = param == null ? null : param.getAccount();
        // 从参数中提取密码
        String password = param == null ? null : param.getPassword();
        
        // 获取客户端IP和User-Agent
        String clientIp = getClientIp();
        // 获取User-Agent信息
        String userAgent = getUserAgent();
        // 根据IP获取地理位置信息
        String region = ipLocationService.getLocationByIp(clientIp);
        
        // 参数校验：账号和密码不能为空
        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            log.warn("登录失败: 账号或密码为空");
            // 记录登录失败日志
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "账号或密码为空");
            return R.fail(CommonPrompt.ACCOUNT_OR_PASSWORD_EMPTY);
        }
        
        // 如果启用了传输加密（SM2），优先尝试解密入参密码
        CryptoTransportConfig cryptoCfg = configService.getJson("security.crypto.transport", CryptoTransportConfig.class, null);
        if (cryptoCfg != null && StringUtils.hasText(cryptoCfg.getPrivateKey()) && "SM2".equalsIgnoreCase(cryptoCfg.getAlgorithm())) {
            try {
                // 创建SM2加密对象
                SM2 sm2 = new SM2(cryptoCfg.getPrivateKey(), cryptoCfg.getPublicKey());
                // 获取密文格式
                String cipherFmt = cryptoCfg.getCipher();
                // 根据密文格式解密
                if ("BCD".equalsIgnoreCase(cipherFmt)) {
                    // BCD格式解密
                    byte[] plain = sm2.decryptFromBcd(password, KeyType.PrivateKey);
                    password = new String(plain, StandardCharsets.UTF_8);
                } else {
                    // 默认格式解密
                    password = sm2.decryptStr(password, KeyType.PrivateKey);
                }
            } catch (Exception ignored) {
                // 解密失败，使用原始密码
            }
        }
        
        // 查询用户信息
        String idKey = account;
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId, SysUser::getAccount, SysUser::getUsername, SysUser::getPassword, SysUser::getEmail, SysUser::getPhone, SysUser::getStatus)
                .eq(SysUser::getAccount, idKey));
        // 校验用户是否存在
        if (user == null) {
            log.warn("登录失败: 用户不存在, account={}", idKey);
            // 记录登录失败日志
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "用户不存在");
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }
        
        // 验证密码：根据策略执行（sm2 可解密存储 / bcrypt 哈希）
        PasswordPolicyConfig policy = configService.getJson("security.password.policy", PasswordPolicyConfig.class, null);
        // 获取密码存储策略，默认为bcrypt
        String store = policy == null ? "bcrypt" : policy.getStore();
        // 根据策略获取密码提供者
        CryptoPasswordProvider provider = CryptoProviders.resolve(store, configService);
        // 验证密码是否正确
        boolean passOk = provider.verify(password, user.getPassword());
        if (!passOk) {
            log.warn("登录失败: 密码不正确, account={}", account);
            // 记录登录失败日志
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "密码不正确");
            return R.fail(CommonPrompt.PASSWORD_INCORRECT);
        }
        
        // 验证码校验（由配置决定方式）
        CaptchaConfig cfg = configService.getJson("login.captcha", CaptchaConfig.class, CaptchaConfig.defaults());
        // 获取验证码模式
        String mode = cfg.getMode();
        // 图片验证码模式
        if ("image".equalsIgnoreCase(mode)) {
            // 获取验证码ID
            String captchaId = param.getCaptchaId();
            // 获取验证码
            String captcha = param.getCaptcha();
            if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captcha)) {
                log.warn("登录失败: 图片验证码缺失, account={}", account);
                // 记录登录失败日志
                loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "验证码缺失");
                return R.fail(CommonPrompt.VERIFICATION_CODE_CANNOT_BE_EMPTY);
            }
            if (!captchaService.verifyImage(captchaId, captcha)) {
                log.warn("登录失败: 图片验证码错误, account={}", account);
                // 记录登录失败日志
                loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "验证码错误");
                return R.fail(CommonPrompt.VERIFICATION_CODE_INCORRECT);
            }
        } else if ("slider".equalsIgnoreCase(mode)) {
            // 滑块验证码模式
            // 获取滑块令牌
            String token = param.getCaptcha();
            // 校验令牌是否为空
            if (!StringUtils.hasText(token)) {
                log.warn("登录失败: 滑块令牌缺失, account={}", account);
                // 记录登录失败日志
                loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "验证码缺失");
                return R.fail(CommonPrompt.VERIFICATION_CODE_CANNOT_BE_EMPTY);
            }
            if (!captchaService.verifySlider(token)) {
                log.warn("登录失败: 滑块令牌校验失败, account={}", account);
                // 记录登录失败日志
                loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "验证码错误");
                return R.fail(CommonPrompt.VERIFICATION_CODE_INCORRECT);
            }
        }
        
        // 查询用户绑定的租户ID列表
        List<SysUserTenant> binds = userTenantMapper.selectList(new LambdaQueryWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, user.getId())
                .orderByDesc(SysUserTenant::getPrefOrder)
                .orderByDesc(SysUserTenant::getLastUsed));
        // 初始化租户ID列表
        List<Long> tenantIds = new ArrayList<>();
        // 提取租户ID
        for (SysUserTenant b : binds) tenantIds.add(b.getTenantId());
        // 校验是否绑定租户
        if (tenantIds.isEmpty()) {
            log.warn("登录提示: 用户未绑定任何租户, account={}", account);
            return R.ok(Collections.emptyList());
        }
        // 查询对应租户信息
        List<SysTenant> tenants = tenantMapper.selectList(new LambdaQueryWrapper<SysTenant>()
                .in(SysTenant::getId, tenantIds));
        // 初始化租户VO列表
        List<TenantVO> vos = new ArrayList<>();
        // 将租户实体转换为VO
        for (SysTenant t : tenants) {
            // 创建租户VO对象
            TenantVO vo = new TenantVO();
            // 设置租户ID
            vo.setId(t.getId() == null ? null : String.valueOf(t.getId()));
            // 设置租户名称
            vo.setName(t.getTenantName());
            // 设置租户描述
            vo.setIntro(t.getDescription());
            // 设置租户Logo
            vo.setLogo(t.getLogo());
            // 设置租户类型
            vo.setTenantType(t.getTenantType());
            // 设置默认标记
            for (SysUserTenant b : binds) { if (b.getTenantId().equals(t.getId())) { vo.setIsDefault(b.getIsDefault()); break; } }
            // 添加到列表
            vos.add(vo);
        }
        log.info("预登录成功（身份校验通过）: account={}, tenants={}", idKey, vos.size());
        return R.ok(vos);
    }

    /**
     * 选择租户，设置上下文。
     * <p>
     * 完整流程：
     * </p>
     * <ol>
     *   <li>参数提取：从参数中提取租户 ID 和账号</li>
     *   <li>参数校验：检查租户 ID 和账号是否为空</li>
     *   <li>用户查询：根据账号查询用户信息</li>
     *   <li>绑定关系校验：检查用户与租户的绑定关系</li>
     *   <li>SaToken 登录：调用 StpUtil.login() 进行登录</li>
     *   <li>会话设置：设置用户 ID、租户 ID、账号到 SaSession</li>
     *   <li>Token 获取：获取 StpUtil 生成的 Token</li>
     *   <li>Redis 存储：将登录上下文存入 Redis，与 Token 过期时间一致</li>
     *   <li>租户上下文设置：调用 TenantContext.set() 设置当前租户</li>
     *   <li>更新绑定关系：更新用户租户绑定的最后使用时间和偏好顺序</li>
     *   <li>记录登录日志：记录登录成功日志</li>
     *   <li>更新用户信息：更新用户最后登录 IP、时间和区域</li>
     *   <li>在线用户缓存：添加在线用户缓存到 Redis</li>
     *   <li>返回用户信息：构建并返回用户 DTO</li>
     * </ol>
     * <p>
     * 数据存储：
     * </p>
     * <ul>
     *   <li>SaSession：存储 LOGIN_USER_ID、LOGIN_TENANT_ID、LOGIN_ACCOUNT</li>
     *   <li>Redis fx:login:ctx:{token}：存储 userId、tenantId、account</li>
     *   <li>Redis fx:online:user:{tenantId}:{userId}：存储在线用户信息</li>
     * </ul>
     *
     * @param param 选择租户参数，包含账号和租户 ID
     * @return {@link R} 包含当前登录用户信息的统一返回结构
     * @throws BusinessException 当参数校验失败、用户不存在、租户未绑定时抛出
     * @see com.forgex.auth.domain.param.TenantChoiceParam
     * @see com.forgex.auth.domain.dto.SysUserDTO
     * @see com.forgex.common.tenant.TenantContext
     * @see cn.dev33.satoken.stp.StpUtil
     */
    @Override
    public R<SysUserDTO> chooseTenant(TenantChoiceParam param) {
        // 从参数中提取租户ID
        Long tenantId = param == null ? null : param.getTenantId();
        String account = param == null ? null : param.getAccount();
        if (tenantId == null) {
            return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.NOT_LOGIN);
        }
        if (!StringUtils.hasText(account)) return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.ACCOUNT_CANNOT_BE_EMPTY);
        String idKey = account;
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, idKey));
        if (user == null) return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.USER_NOT_FOUND);
        SysUserTenant bind = userTenantMapper.selectOne(new LambdaQueryWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, user.getId())
                .eq(SysUserTenant::getTenantId, tenantId)
                .last("limit 1"));
        if (bind == null) return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.USER_NOT_BOUND_TO_TENANT);
        // 调用SaToken进行登录
        StpUtil.login(idKey);
        // 获取SaSession对象
        SaSession session = StpUtil.getSession();
        // 设置会话信息
        if (session != null) {
            // 设置登录用户ID
            session.set("LOGIN_USER_ID", user.getId());
            // 设置登录租户ID
            session.set("LOGIN_TENANT_ID", tenantId);
            // 设置登录账号
            session.set("LOGIN_ACCOUNT", idKey);
        }
        // 获取Token值
        String token = StpUtil.getTokenValue();
        // 将登录上下文存入Redis
        if (StringUtils.hasText(token)) {
            // 创建上下文Map
            Map<String, Object> ctx = new HashMap<>();
            // 设置用户ID
            ctx.put("userId", user.getId());
            // 设置租户ID
            ctx.put("tenantId", tenantId);
            // 设置账号
            ctx.put("account", idKey);
            // 转换为JSON字符串
            String json = JSONUtil.toJsonStr(ctx);
            // 获取Token超时时间
            long timeout = StpUtil.getTokenTimeout();
            // 构造Redis键
            String key = "fx:login:ctx:" + token;
            // 设置Redis键值对
            if (timeout > 0) {
                // 设置带过期时间的键值对
                redis.opsForValue().set(key, json, Duration.ofSeconds(timeout));
            } else {
                // 设置永不过期的键值对
                redis.opsForValue().set(key, json);
            }
        }
        // 设置租户上下文
        TenantContext.set(tenantId);
        // 更新用户租户绑定关系
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
        
        // 添加在线用户缓存（使用userId+tenantId作为key）
        String onlineKey = "fx:online:user:" + tenantId + ":" + user.getId();
        try {
            // 创建在线用户信息Map
            Map<String, Object> onlineInfo = new HashMap<>();
            // 设置用户ID
            onlineInfo.put("userId", user.getId());
            // 设置租户ID
            onlineInfo.put("tenantId", tenantId);
            // 设置账号
            onlineInfo.put("account", idKey);
            // 设置token
            onlineInfo.put("token", token);
            // 设置登录时间
            onlineInfo.put("loginTime", LocalDateTime.now().toString());
            // 设置客户端IP
            onlineInfo.put("clientIp", clientIp);
            // 设置User-Agent
            onlineInfo.put("userAgent", userAgent);
            // 转换为JSON字符串
            String onlineJson = JSONUtil.toJsonStr(onlineInfo);
            // 获取Token超时时间
            long timeout = StpUtil.getTokenTimeout();
            // 设置Redis键值对，与token过期时间一致
            if (timeout > 0) {
                redis.opsForValue().set(onlineKey, onlineJson, Duration.ofSeconds(timeout));
            } else {
                redis.opsForValue().set(onlineKey, onlineJson);
            }
            log.info("添加在线用户缓存: userId={}, tenantId={}, token={}", user.getId(), tenantId, token);
        } catch (Exception e) {
            log.warn("添加在线用户缓存失败: userId={}, tenantId={}", user.getId(), tenantId, e);
        }
        
        log.info("选择租户成功: account={}, tenantId={}", account, tenantId);
        // 创建用户DTO对象
        SysUserDTO result = new SysUserDTO();
        // 设置用户ID
        result.setId(user.getId());
        // 设置账号
        result.setAccount(user.getAccount());
        // 设置用户名
        result.setUsername(user.getUsername());
        // 设置邮箱
        result.setEmail(user.getEmail());
        // 设置手机号
        result.setPhone(user.getPhone());
        // 设置头像
        result.setAvatar(user.getAvatar());
        // 设置状态
        result.setStatus(user.getStatus());
        // 设置租户ID
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
     *
     * @param account 用户账号
     * @param ordered 租户ID排序列表
     * @param defaultTenantId 默认租户ID
     * @return 更新结果
     */
    @Override
    public R<Boolean> updateTenantPreferences(String account, java.util.List<Long> ordered, Long defaultTenantId) {
        if (!StringUtils.hasText(account) || ordered == null) return R.fail(CommonPrompt.BAD_REQUEST);
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getAccount, account));
        if (user == null) return R.fail(CommonPrompt.USER_NOT_FOUND);
        // 计算权重（排序列表长度）
        int n = ordered.size(); int weight = n;
        // 遍历排序列表，更新每个租户的偏好顺序
        for (Long tid : ordered) {
            // 更新租户偏好顺序和默认标记
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

    @Override
    public R<Boolean> changeLanguage(String lang) {
        // 参数校验：语言不能为空
        if (!StringUtils.hasText(lang)) {
            return R.fail(500, AuthPromptEnum.LANG_EMPTY);
        }
        // 获取当前用户ID
        Long userId = CurrentUserUtils.getUserId();
        // 获取当前租户ID
        Long tenantId = CurrentUserUtils.getTenantId();
        // 校验用户是否登录
        if (userId == null || tenantId == null) {
            return R.fail(401, AuthPromptEnum.NOT_LOGIN);
        }

        // 构造语言偏好键
        String prefKey = "fx:lang:" + tenantId + ":" + userId;
        try {
            // 设置语言偏好到Redis
            redis.opsForValue().set(prefKey, lang);
        } catch (Exception e) {
            // 设置失败
            return R.fail(500, AuthPromptEnum.LANG_SET_FAILED);
        }

        // 更新登录上下文中的语言设置
        String token = StpUtil.getTokenValue();
        if (StringUtils.hasText(token)) {
            String ctxKey = "fx:login:ctx:" + token;
            try {
                // 创建上下文Map
                Map<String, Object> ctx = new HashMap<>();
                // 获取原有上下文
                String raw = redis.opsForValue().get(ctxKey);
                if (StringUtils.hasText(raw)) {
                    try {
                        // 合并原有上下文
                        ctx.putAll(JSONUtil.parseObj(raw));
                    } catch (Exception ignored) {
                        // 合并失败，继续使用新上下文
                    }
                }
                // 设置用户ID
                ctx.put("userId", userId);
                // 设置租户ID
                ctx.put("tenantId", tenantId);
                // 获取账号
                String account = CurrentUserUtils.getAccount();
                if (StringUtils.hasText(account)) {
                    // 设置账号
                    ctx.put("account", account);
                }
                // 设置语言
                ctx.put("lang", lang);
                // 转换为JSON字符串
                String json = JSONUtil.toJsonStr(ctx);
                // 获取Token超时时间
                long timeout = StpUtil.getTokenTimeout();
                if (timeout > 0) {
                    // 设置带过期时间的键值对
                    redis.opsForValue().set(ctxKey, json, Duration.ofSeconds(timeout));
                } else {
                    // 设置永不过期的键值对
                    redis.opsForValue().set(ctxKey, json);
                }
            } catch (Exception ignored) {
                // 更新失败，不影响主流程
            }
        }
        return R.ok(true);
    }


    /**
     * 管理员权限校验
     * <p>逻辑：检查是否拥有 admin 角色</p>
     *
     * @return 校验结果
     * @see cn.dev33.satoken.stp.StpUtil#checkRole(String)
     */
    @Override
    @Deprecated
    public R<Boolean> secureAdmin() {
        // 检查当前用户是否拥有admin角色
        StpUtil.checkLogin();
        return R.fail(CommonPrompt.BAD_REQUEST);
    }

    /**
     * 根据用户ID重置密码
     * <p>将指定用户的密码重置为默认密码 "123456"，使用 BCrypt 算法进行哈希存储。</p>
     *
     * @param userId 用户ID
     * @return 重置结果
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
        String hashed = BCrypt.hashpw("123456");
        int n = userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getPassword, hashed)
                .eq(SysUser::getId, userId));
        if (n > 0) {
            return R.ok(true);
        }
        return R.fail(CommonPrompt.RESET_FAILED);
    }

    /**
     * 用户登出
     * <p>清除用户登录状态，删除Redis中的登录上下文信息，并移除租户上下文。</p>
     *
     * @return 登出结果
     * @see StpUtil#logout()
     */
    @Override
    public R<Boolean> logout() {
        try {
            // 获取当前登录用户ID（使用安全方法，不会抛异常）
            Object uid = StpUtil.getLoginIdDefaultNull();
            
            // 初始化用户ID和租户ID
            Long userId = null;
            Long tenantId = null;
            String account = null;
            String tokenValue = null;
            
            // 尝试获取Token值（可能为null）
            try {
                tokenValue = StpUtil.getTokenValue();
            } catch (Exception e) {
                log.debug("获取Token失败，可能用户未登录: {}", e.getMessage());
            }
            
            // 尝试获取SaSession对象
            try {
                SaSession session = StpUtil.getSession(false);
                // 从会话中提取用户ID和租户ID
                if (session != null) {
                    // 提取用户ID
                    Object uidObj = session.get("LOGIN_USER_ID");
                    if (uidObj instanceof Long) {
                        userId = (Long) uidObj;
                    } else if (uidObj instanceof Integer) {
                        userId = ((Integer) uidObj).longValue();
                    } else if (uidObj instanceof String) {
                        try { userId = Long.valueOf((String) uidObj); } catch (Exception ignored) { }
                    }
                    // 提取租户ID
                    Object tidObj = session.get("LOGIN_TENANT_ID");
                    if (tidObj instanceof Long) {
                        tenantId = (Long) tidObj;
                    } else if (tidObj instanceof Integer) {
                        tenantId = ((Integer) tidObj).longValue();
                    } else if (tidObj instanceof String) {
                        try { tenantId = Long.valueOf((String) tidObj); } catch (Exception ignored) { }
                    }
                }
            } catch (Exception e) {
                log.debug("获取Session失败，可能会话已过期: {}", e.getMessage());
            }
            
            // 获取账号
            account = uid == null ? null : String.valueOf(uid);

            // 记录登出时间/原因（异步，不影响主流程）
            if (StringUtils.hasText(tokenValue)) {
                try {
                    loginLogService.recordLogoutByToken(tokenValue, com.forgex.common.security.LogoutReason.MANUAL);
                } catch (Exception e) {
                    log.warn("记录登出日志失败: {}", e.getMessage());
                }
            }

            // 删除Redis中的登录上下文
            if (StringUtils.hasText(tokenValue)) {
                String key = "fx:login:ctx:" + tokenValue;
                try {
                    String raw = redis.opsForValue().get(key);
                    if (StringUtils.hasText(raw)) {
                        cn.hutool.json.JSONObject obj = JSONUtil.parseObj(raw);
                        Long ctxUserId = obj.getLong("userId");
                        Long ctxTenantId = obj.getLong("tenantId");
                        if (userId == null && ctxUserId != null) {
                            userId = ctxUserId;
                        }
                        if (tenantId == null && ctxTenantId != null) {
                            tenantId = ctxTenantId;
                        }
                    }
                } catch (Exception e) {
                    log.debug("读取登录上下文失败: {}", e.getMessage());
                }
                try {
                    redis.delete(key);
                } catch (Exception e) {
                    log.warn("删除登录上下文失败: {}", e.getMessage());
                }
            }

            // 删除在线用户缓存
            if (userId != null && tenantId != null) {
                String onlineKey = "fx:online:user:" + tenantId + ":" + userId;
                try {
                    redis.delete(onlineKey);
                    log.info("删除在线用户缓存: userId={}, tenantId={}", userId, tenantId);
                } catch (Exception e) {
                    log.warn("删除在线用户缓存失败: {}", e.getMessage());
                }
            }
            
            // 调用SaToken登出（即使未登录也不会报错）
            try {
                StpUtil.logout();
            } catch (Exception e) {
                log.debug("SaToken登出失败（可能未登录）: {}", e.getMessage());
            }
            
            log.info("退出成功: account={}", account);
            return R.ok(true);
        } catch (Exception e) {
            // 记录详细的异常信息
            log.error("登出过程发生异常", e);
            return R.fail(CommonPrompt.LOGOUT_FAILED);
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

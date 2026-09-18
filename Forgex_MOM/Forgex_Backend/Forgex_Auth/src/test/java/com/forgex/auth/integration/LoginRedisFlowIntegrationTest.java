package com.forgex.auth.integration;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import com.forgex.auth.domain.config.CaptchaConfig;
import com.forgex.auth.domain.entity.SysUser;
import com.forgex.auth.domain.entity.SysUserTenant;
import com.forgex.auth.domain.param.TenantChoiceParam;
import com.forgex.auth.mapper.SysUserMapper;
import com.forgex.auth.mapper.SysUserTenantMapper;
import com.forgex.auth.service.LoginInteractionCodeService;
import com.forgex.auth.service.LoginLogService;
import com.forgex.auth.service.PasswordLifecycleHelper;
import com.forgex.auth.service.TenantSelectionAuthorizationService;
import com.forgex.auth.service.impl.AuthServiceImpl;
import com.forgex.auth.service.impl.CaptchaServiceImpl;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.forgex.common.config.ConfigService;
import com.forgex.common.domain.config.CryptoTransportConfig;
import com.forgex.common.domain.config.LoginSecurityConfig;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.common.security.LoginSessionKeys;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.util.IpLocationService;
import com.forgex.common.web.R;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.MockedStatic;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.Redisson;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 本地 Redis 认证链集成测试。
 * <p>
 * 仅在显式提供 {@code FORGEX_REDIS_INTEGRATION=true} 时运行，使用 Redis DB 15。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@EnabledIfEnvironmentVariable(named = "FORGEX_REDIS_INTEGRATION", matches = "true")
class LoginRedisFlowIntegrationTest {

    private static final int REDIS_DATABASE = 15;

    private RedissonClient redisson;
    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redis;
    private String account;
    private String captchaKey;
    private String onlineKey;
    private String legacyOnlineKey;

    @BeforeAll
    static void initializeMyBatisMetadata() {
        Configuration configuration = new Configuration();
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, "auth-user.xml"), SysUser.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, "auth-user-tenant.xml"), SysUserTenant.class);
    }

    @BeforeEach
    void setUp() {
        account = "integration-" + UUID.randomUUID();
        captchaKey = "captcha:image:" + UUID.randomUUID();
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(REDIS_DATABASE);
        redisson = Redisson.create(config);
        connectionFactory = new LettuceConnectionFactory("127.0.0.1", 6379);
        connectionFactory.setDatabase(REDIS_DATABASE);
        connectionFactory.afterPropertiesSet();
        redis = new StringRedisTemplate(connectionFactory);
        redis.afterPropertiesSet();
    }

    @AfterEach
    void tearDown() {
        if (redisson != null) {
            redisson.getAtomicLong("fx:auth:login:fail:" + account).delete();
            redisson.getBucket("fx:auth:login:lock:" + account).delete();
            if (onlineKey != null) {
                redis.delete(onlineKey);
            }
            if (legacyOnlineKey != null) {
                redis.delete(legacyOnlineKey);
            }
            redisson.shutdown();
        }
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
        TenantContext.clear();
    }

    @Test
    void consumesCaptchaAndInteractionCodeLocksFailuresAndWritesTenantSession() {
        assertTrue(decryptsSm2TransportPassword());
        assertTrue(verifyCaptchaIsSingleUse());
        assertTrue(recordedFailuresLockTheAccount());

        LoginInteractionCodeService interactionCodeService = new LoginInteractionCodeService(redisson);
        TenantSelectionAuthorizationService authorizationService = new TenantSelectionAuthorizationService(interactionCodeService);
        String interactionCode = interactionCodeService.issue(101L, account, "B");

        try (MockedStatic<StpUtil> stp = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(false);
            assertTrue(authorizationService.authorize(101L, account, "B", interactionCode));
            assertFalse(authorizationService.authorize(101L, account, "B", interactionCode));
        }

        String choiceCode = interactionCodeService.issue(101L, account, "B");
        SaSession tokenSession = new SaSession("token-session");
        SaSession loginSession = new SaSession("login-session");
        AuthServiceImpl authService = configuredAuthService(authorizationService);
        TenantChoiceParam choice = new TenantChoiceParam();
        choice.setAccount(account);
        choice.setTenantId(201L);
        choice.setLoginTerminal("B");
        choice.setInteractionCode(choiceCode);

        try (MockedStatic<StpUtil> stp = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(false);
            stp.when(StpUtil::getTokenValue).thenReturn("integration-token");
            stp.when(StpUtil::getTokenSession).thenReturn(tokenSession);
            stp.when(StpUtil::getSession).thenReturn(loginSession);
            stp.when(StpUtil::getTokenTimeout).thenReturn(3600L);

            R<?> result = authService.doChooseTenant(choice);

            assertEquals(200, result.getCode());
            stp.verify(() -> StpUtil.login(account));
        }

        assertEquals(101L, tokenSession.get(LoginSessionKeys.KEY_USER_ID));
        assertEquals(201L, tokenSession.get(LoginSessionKeys.KEY_TENANT_ID));
        assertEquals(account, tokenSession.get(LoginSessionKeys.KEY_ACCOUNT));
        assertEquals(101L, loginSession.get(LoginSessionKeys.KEY_USER_ID));
        assertEquals(201L, TenantContext.get());
    }

    private boolean decryptsSm2TransportPassword() {
        SM2 sm2 = new SM2();
        CryptoTransportConfig transportConfig = new CryptoTransportConfig();
        transportConfig.setAlgorithm("SM2");
        transportConfig.setCipher("BCD");
        transportConfig.setPublicKey(sm2.getPublicKeyBase64());
        transportConfig.setPrivateKey(sm2.getPrivateKeyBase64());

        ConfigService configService = mock(ConfigService.class);
        when(configService.getJson("security.crypto.transport", CryptoTransportConfig.class, null))
                .thenReturn(transportConfig);
        AuthServiceImpl authService = new AuthServiceImpl();
        ReflectionTestUtils.setField(authService, "configService", configService);

        String plainPassword = "Integration-Passw0rd!";
        String encryptedPassword = sm2.encryptBcd(plainPassword, KeyType.PublicKey);
        String decryptedPassword = ReflectionTestUtils.invokeMethod(
                authService, "decryptTransportPassword", encryptedPassword);

        return plainPassword.equals(decryptedPassword);
    }

    private boolean verifyCaptchaIsSingleUse() {
        CaptchaServiceImpl captchaService = new CaptchaServiceImpl();
        ConfigService configService = mock(ConfigService.class);
        when(configService.getJson("login.captcha", CaptchaConfig.class, CaptchaConfig.defaults()))
                .thenReturn(CaptchaConfig.defaults());
        ReflectionTestUtils.setField(captchaService, "redis", redis);
        ReflectionTestUtils.setField(captchaService, "configService", configService);
        redis.opsForValue().set(captchaKey, "code");

        assertTrue(captchaService.verifyImage(captchaKey.substring("captcha:image:".length()), "CODE"));
        return !captchaService.verifyImage(captchaKey.substring("captcha:image:".length()), "CODE");
    }

    private boolean recordedFailuresLockTheAccount() {
        AuthServiceImpl authService = new AuthServiceImpl();
        ReflectionTestUtils.setField(authService, "redissonClient", redisson);
        LoginSecurityConfig config = new LoginSecurityConfig();
        config.setFailWindowMinutes(1);
        config.setMaxFailCount(2);
        config.setLockMinutes(1);

        ReflectionTestUtils.invokeMethod(authService, "recordLoginFailureState", account, config);
        ReflectionTestUtils.invokeMethod(authService, "recordLoginFailureState", account, config);

        return redisson.getBucket("fx:auth:login:lock:" + account).isExists()
                && !redisson.getAtomicLong("fx:auth:login:fail:" + account).isExists();
    }

    private AuthServiceImpl configuredAuthService(TenantSelectionAuthorizationService authorizationService) {
        AuthServiceImpl service = new AuthServiceImpl();
        SysUserMapper userMapper = mock(SysUserMapper.class);
        SysUserTenantMapper userTenantMapper = mock(SysUserTenantMapper.class);
        SysUser user = new SysUser();
        user.setId(101L);
        user.setAccount(account);
        user.setUsername("integration user");
        user.setStatus(true);
        SysUserTenant binding = new SysUserTenant();
        binding.setUserId(101L);
        binding.setTenantId(201L);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(userTenantMapper.selectOne(any())).thenReturn(binding);

        ConfigService configService = mock(ConfigService.class);
        PasswordPolicyConfig passwordPolicy = new PasswordPolicyConfig();
        when(configService.getJson("security.password.policy", PasswordPolicyConfig.class, null))
                .thenReturn(passwordPolicy);
        PasswordLifecycleHelper passwordLifecycle = mock(PasswordLifecycleHelper.class);
        when(passwordLifecycle.isExpired(any(), any())).thenReturn(false);
        when(passwordLifecycle.expireInDays(any(), any())).thenReturn(90);

        ReflectionTestUtils.setField(service, "userMapper", userMapper);
        ReflectionTestUtils.setField(service, "userTenantMapper", userTenantMapper);
        ReflectionTestUtils.setField(service, "configService", configService);
        ReflectionTestUtils.setField(service, "redis", redis);
        ReflectionTestUtils.setField(service, "redissonClient", redisson);
        ReflectionTestUtils.setField(service, "ipLocationService", mock(IpLocationService.class));
        ReflectionTestUtils.setField(service, "loginLogService", mock(LoginLogService.class));
        ReflectionTestUtils.setField(service, "tenantSelectionAuthorizationService", authorizationService);
        ReflectionTestUtils.setField(service, "passwordLifecycleHelper", passwordLifecycle);
        onlineKey = "fx:online:user:201:101:integration-token";
        legacyOnlineKey = "fx:online:user:201:101";
        return service;
    }
}

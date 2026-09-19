package com.forgex.auth.mfa;

import com.forgex.auth.domain.entity.SysUserMfa;
import com.forgex.auth.mapper.SysTenantMapper;
import com.forgex.auth.mapper.SysUserMapper;
import com.forgex.auth.mapper.SysUserMfaMapper;
import com.forgex.auth.mapper.SysUserTenantMapper;
import com.forgex.auth.service.LoginInteractionCodeService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MFA 绑定服务测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class MfaServiceTest {

    /**
     * 绑定应返回 otpauth URI 与 10 个恢复码。
     */
    @Test
    void bindReturnsOtpAuthUriAndRecoveryCodes() {
        TotpService totpService = new TotpService();
        SysUserMfaMapper mfaMapper = mock(SysUserMfaMapper.class);
        when(mfaMapper.selectOne(any())).thenReturn(null);
        MfaServiceImpl service = new MfaServiceImpl(
                totpService,
                mfaMapper,
                mock(SysUserMapper.class),
                mock(SysUserTenantMapper.class),
                mock(SysTenantMapper.class),
                mock(LoginInteractionCodeService.class),
                mock(RedissonClient.class)
        );

        Map<String, Object> result = service.bind(1L, "admin");
        String uri = String.valueOf(result.get("otpAuthUri"));
        assertTrue(uri.startsWith("otpauth://totp/Forgex:admin?secret="));
        assertTrue(uri.contains("issuer=Forgex"));
        assertTrue(((java.util.List<?>) result.get("recoveryCodes")).size() == 10);
    }

    /**
     * 错误动态码确认绑定应失败。
     */
    @Test
    void confirmBindRejectsInvalidCode() {
        TotpService totpService = new TotpService();
        SysUserMfaMapper mfaMapper = mock(SysUserMfaMapper.class);
        SysUserMfa entity = new SysUserMfa();
        entity.setSecretCiphertext(totpService.generateSecret());
        when(mfaMapper.selectOne(any())).thenReturn(entity);
        MfaServiceImpl service = new MfaServiceImpl(
                totpService,
                mfaMapper,
                mock(SysUserMapper.class),
                mock(SysUserTenantMapper.class),
                mock(SysTenantMapper.class),
                mock(LoginInteractionCodeService.class),
                mock(RedissonClient.class)
        );
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> service.confirmBind(1L, "000000"));
    }
}

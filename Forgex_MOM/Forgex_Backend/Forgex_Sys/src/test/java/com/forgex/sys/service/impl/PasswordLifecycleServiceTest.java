package com.forgex.sys.service.impl;

import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserPasswordHistory;
import com.forgex.sys.mapper.SysUserPasswordHistoryMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 口令生命周期复用检测测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class PasswordLifecycleServiceTest {

    /**
     * 当前口令复用应被拒绝。
     */
    @Test
    void rejectCurrentPasswordReuse() {
        SysUserPasswordHistoryMapper mapper = mock(SysUserPasswordHistoryMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        PasswordLifecycleServiceImpl service = new PasswordLifecycleServiceImpl(mapper);
        SysUser user = new SysUser();
        user.setId(1L);
        user.setPassword("hashed-old");
        CryptoPasswordProvider provider = mock(CryptoPasswordProvider.class);
        when(provider.verify("old", "hashed-old")).thenReturn(true);
        PasswordPolicyConfig policy = new PasswordPolicyConfig();
        policy.setHistoryCount(5);
        assertFalse(service.assertNotReusedAndRecord(user, "old", provider, policy));
    }

    /**
     * 新口令可通过并写入历史。
     */
    @Test
    void acceptFreshPassword() {
        SysUserPasswordHistoryMapper mapper = mock(SysUserPasswordHistoryMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        PasswordLifecycleServiceImpl service = new PasswordLifecycleServiceImpl(mapper);
        SysUser user = new SysUser();
        user.setId(1L);
        user.setTenantId(0L);
        user.setPassword("hashed-old");
        CryptoPasswordProvider provider = mock(CryptoPasswordProvider.class);
        when(provider.verify(any(), any())).thenReturn(false);
        PasswordPolicyConfig policy = new PasswordPolicyConfig();
        policy.setHistoryCount(5);
        assertTrue(service.assertNotReusedAndRecord(user, "brand-new", provider, policy));
    }
}

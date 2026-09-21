package com.forgex.sys.dataperm;

import com.forgex.common.dataperm.DataPermissionHelper.DataPermissionInfo;
import com.forgex.common.dataperm.DataScope;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.UserContext;
import com.forgex.sys.service.platform.BasicDataScopeService;
import com.forgex.sys.service.platform.BasicPlatformRequestGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 内部权限接口必须使用已验证会话，禁止查询其他用户。 */
class BasicDataScopeServiceTest {
    private final BasicPlatformRequestGuard guard = mock(BasicPlatformRequestGuard.class);
    private final SysDataScopeProvider provider = mock(SysDataScopeProvider.class);
    private final BasicDataScopeService service = new BasicDataScopeService(guard, provider);

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void returnsValidatedTenantAndScope() {
        UserContext.set(42L);
        when(guard.requireTenant()).thenReturn(1L);
        when(provider.load(42L)).thenReturn(new DataPermissionInfo(DataScope.DEPT, Set.of(7L), 42L));
        var result = service.load(42L);
        assertEquals(1L, result.tenantId());
        assertEquals(42L, result.userId());
        assertEquals("DEPT", result.dataScope());
        assertEquals(Set.of(7L), result.deptIds());
    }

    @Test
    void refusesDifferentOrMissingUser() {
        UserContext.set(42L);
        when(guard.requireTenant()).thenReturn(1L);
        assertThrows(I18nBusinessException.class, () -> service.load(99L));
        assertThrows(I18nBusinessException.class, () -> service.load(null));
        verifyNoInteractions(provider);
    }

    @Test
    void refusesInvalidSessionBeforeLoadingScope() {
        when(guard.requireTenant()).thenThrow(new IllegalStateException("invalid session"));
        assertThrows(IllegalStateException.class, () -> service.load(42L));
        verifyNoInteractions(provider);
    }
}

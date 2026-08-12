package com.forgex.sys.service.impl;

import com.forgex.common.config.ConfigService;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.enums.FxExcelImportMode;
import com.forgex.common.tenant.TenantContext;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserTenant;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserProfileMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.mapper.SysUserTenantMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SysUserImportProxyHandlerTest {

    private SysUserMapper userMapper;
    private SysUserTenantMapper userTenantMapper;
    private ConfigService configService;
    private SysUserImportProxyHandler handler;

    @BeforeEach
    void setUp() {
        userMapper = mock(SysUserMapper.class);
        userTenantMapper = mock(SysUserTenantMapper.class);
        configService = mock(ConfigService.class);
        handler = new SysUserImportProxyHandler(
            userMapper,
            userTenantMapper,
            mock(SysUserRoleMapper.class),
            mock(SysUserProfileMapper.class),
            configService
        );
        TenantContext.set(100L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void handleShouldCreateImportedUserInRegisteredHandler() {
        when(configService.getJson(eq("security.password.policy"), eq(PasswordPolicyConfig.class), any(PasswordPolicyConfig.class)))
            .thenAnswer(invocation -> invocation.getArgument(2));
        doAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        }).when(userMapper).insert(any(SysUser.class));

        FxExcelImportResultDTO result = handler.handle(importParam(Map.of(
            "account", "u001",
            "username", "User One",
            "phone", "13800000000",
            "email", "u001@example.com",
            "employeeId", 10
        )));

        assertEquals(1, result.getTotalCount());
        assertEquals(1, result.getCreatedCount());
        verify(userMapper).insert(any(SysUser.class));
        verify(userTenantMapper).insert(any(SysUserTenant.class));
    }

    @Test
    void handleShouldMarkBlankAccountAsFailed() {
        FxExcelImportResultDTO result = handler.handle(importParam(Map.of("account", "")));

        assertEquals(1, result.getTotalCount());
        assertEquals(1, result.getFailedCount());
        verify(userMapper, never()).insert(any(SysUser.class));
    }

    private FxExcelImportExecuteParam importParam(Map<String, Object> row) {
        FxExcelImportExecuteParam param = new FxExcelImportExecuteParam();
        param.setImportMode(FxExcelImportMode.ADD.name());
        param.setImportData(Map.of("main", List.of(row)));
        return param;
    }
}

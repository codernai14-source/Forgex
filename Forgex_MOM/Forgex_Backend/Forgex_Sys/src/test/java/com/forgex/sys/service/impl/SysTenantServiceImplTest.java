package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.dynamic.datasource.tx.TransactionContext;
import com.forgex.common.enums.TenantTypeEnum;
import com.forgex.common.service.TemplateMessageService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.sys.domain.dto.tenant.TenantInitResult;
import com.forgex.sys.domain.dto.tenant.SysTenantSaveParam;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysTenant;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysTenantMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.service.ITenantInitService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SysTenantServiceImplTest {

    private SysTenantMapper tenantMapper;
    private ITenantInitService tenantInitService;
    private SysRoleMapper roleMapper;
    private SysUserRoleMapper userRoleMapper;
    private SysUserMapper userMapper;
    private TemplateMessageService templateMessageService;
    private SysTenantServiceImpl service;

    @BeforeEach
    void setUp() {
        tenantMapper = mock(SysTenantMapper.class);
        tenantInitService = mock(ITenantInitService.class);
        roleMapper = mock(SysRoleMapper.class);
        userRoleMapper = mock(SysUserRoleMapper.class);
        userMapper = mock(SysUserMapper.class);
        templateMessageService = mock(TemplateMessageService.class);
        service = new SysTenantServiceImpl(
                tenantMapper,
                tenantInitService,
                roleMapper,
                userRoleMapper,
                userMapper,
                templateMessageService);
        when(tenantMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        TransactionContext.removeSynchronizations();
        String xid = TransactionContext.getXID();
        if (xid != null) {
            TransactionContext.unbind(xid);
        }
    }

    @Test
    void childTenantWithoutParentIsRejectedBeforeInsert() {
        SysTenantSaveParam param = childTenantParam(null);

        assertThrows(IllegalArgumentException.class, () -> service.create(param));

        verify(tenantMapper, never()).insert(any(SysTenant.class));
        verify(tenantInitService, never()).initTenant(any(), any(), any(), any());
    }

    @Test
    void childTenantWithNonMainParentIsRejectedBeforeInsert() {
        SysTenant parent = new SysTenant();
        parent.setId(9L);
        parent.setTenantType(TenantTypeEnum.CUSTOMER_TENANT);
        parent.setDeleted(false);
        when(tenantMapper.selectById(9L)).thenReturn(parent);

        assertThrows(IllegalArgumentException.class, () -> service.create(childTenantParam(9L)));

        verify(tenantMapper, never()).insert(any(SysTenant.class));
    }

    @Test
    void initializationFailureIsPropagatedAndNotificationIsSkipped() {
        SysTenant parent = new SysTenant();
        parent.setId(1L);
        parent.setTenantType(TenantTypeEnum.MAIN_TENANT);
        parent.setDeleted(false);
        when(tenantMapper.selectById(1L)).thenReturn(parent);
        org.mockito.Mockito.doAnswer(invocation -> {
            SysTenant tenant = invocation.getArgument(0);
            tenant.setId(100L);
            return 1;
        }).when(tenantMapper).insert(any(SysTenant.class));
        when(tenantInitService.initTenant(100L, "测试租户", "test", TenantTypeEnum.CUSTOMER_TENANT))
                .thenThrow(new IllegalStateException("初始化失败"));

        assertThrows(IllegalStateException.class, () -> service.create(childTenantParam(1L)));

        verify(templateMessageService, never()).sendByTemplate(any(), any(), any(), any());
    }

    @Test
    void successfulCreationNotifiesParentAdministratorsAndRestoresTenantContext() {
        SysTenant parent = mainTenant(1L);
        when(tenantMapper.selectById(1L)).thenReturn(parent);
        org.mockito.Mockito.doAnswer(invocation -> {
            SysTenant tenant = invocation.getArgument(0);
            tenant.setId(100L);
            return 1;
        }).when(tenantMapper).insert(any(SysTenant.class));
        when(tenantInitService.initTenant(100L, "测试租户", "test", TenantTypeEnum.CUSTOMER_TENANT))
                .thenReturn(new TenantInitResult(30L, "admin_test_0100", "Initial#123"));

        SysRole role = new SysRole();
        role.setId(10L);
        role.setTenantId(1L);
        role.setRoleKey("admin");
        role.setStatus(true);
        role.setDeleted(false);
        when(roleMapper.selectList(any(Wrapper.class))).thenAnswer(invocation -> {
            assertEquals(1L, TenantContext.get());
            return List.of(role);
        });

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(20L);
        userRole.setRoleId(10L);
        userRole.setTenantId(1L);
        when(userRoleMapper.selectList(any(Wrapper.class))).thenAnswer(invocation -> {
            assertEquals(1L, TenantContext.get());
            return List.of(userRole);
        });

        SysUser administrator = new SysUser();
        administrator.setId(20L);
        administrator.setTenantId(1L);
        administrator.setStatus(true);
        administrator.setDeleted(false);
        when(userMapper.selectList(any(Wrapper.class))).thenAnswer(invocation -> {
            assertEquals(1L, TenantContext.get());
            return List.of(administrator);
        });
        when(templateMessageService.sendByTemplate(any(), any(), any(), any())).thenAnswer(invocation -> {
            assertEquals(1L, TenantContext.get());
            return 1;
        });

        TenantContext.set(88L);
        TransactionContext.bind("tenant-create-test-xid");
        Long tenantId = service.create(childTenantParam(1L));

        assertEquals(100L, tenantId);
        assertEquals(88L, TenantContext.get());
        verify(templateMessageService, never()).sendByTemplate(any(), any(), any(), any());
        List<TransactionSynchronization> synchronizations = TransactionContext.getSynchronizations();
        assertEquals(1, synchronizations.size());
        synchronizations.get(0).afterCommit();
        assertEquals(88L, TenantContext.get());
        ArgumentCaptor<List<Long>> receiverCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(templateMessageService).sendByTemplate(
                eq("SYS_TENANT_CREATED"), receiverCaptor.capture(), dataCaptor.capture(), eq("TENANT_CREATED"));
        assertEquals(List.of(20L), receiverCaptor.getValue());
        assertEquals("测试租户", dataCaptor.getValue().get("tenantName"));
        assertEquals("test", dataCaptor.getValue().get("tenantCode"));
        assertEquals("admin_test_0100", dataCaptor.getValue().get("administratorAccount"));
        assertEquals("Initial#123", dataCaptor.getValue().get("initialPassword"));
    }

    private SysTenant mainTenant(Long id) {
        SysTenant tenant = new SysTenant();
        tenant.setId(id);
        tenant.setTenantType(TenantTypeEnum.MAIN_TENANT);
        tenant.setDeleted(false);
        return tenant;
    }

    private SysTenantSaveParam childTenantParam(Long parentTenantId) {
        SysTenantSaveParam param = new SysTenantSaveParam();
        param.setTenantName("测试租户");
        param.setTenantCode("test");
        param.setTenantType(TenantTypeEnum.CUSTOMER_TENANT);
        param.setParentTenantId(parentTenantId);
        param.setStatus(true);
        return param;
    }
}

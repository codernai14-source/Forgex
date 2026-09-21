package com.forgex.basic.dataperm;

import com.forgex.common.api.feign.SysBasicSupportFeignClient;
import com.forgex.common.api.dto.SysDataScopeDTO;
import com.forgex.common.dataperm.DataPermissionHelper;
import com.forgex.common.dataperm.DataPermissionInterceptor;
import com.forgex.common.dataperm.DataScopeProvider;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Basic 服务的数据权限提供者装配回归测试。 */
class BasicDataScopeProviderTest {

    @AfterEach
    void clearContext() {
        UserContext.clear();
        TenantContext.clear();
        DataPermissionHelper.clearAllCache();
    }

    @Test
    void appliesRemoteDepartmentScopeToEmployeeQuery() throws Exception {
        var client = mock(SysBasicSupportFeignClient.class);
        when(client.dataScope(42L)).thenReturn(R.ok(new SysDataScopeDTO(1L, 42L, "DEPT", Set.of(7L))));
        UserContext.set(42L);
        TenantContext.set(1L);
        try (var context = context(client)) {
            var interceptor = new DataPermissionInterceptor(context.getBeanProvider(DataScopeProvider.class),
                    context.getBeanProvider(com.forgex.common.dataperm.SubjectSecurityLevelProvider.class));
            var configuration = new Configuration();
            String sql = "SELECT id FROM basic_employee WHERE deleted = 0";
            var statement = new MappedStatement.Builder(configuration,
                    "com.forgex.basic.employee.mapper.BasicEmployeeMapper.selectList",
                    new StaticSqlSource(configuration, sql), SqlCommandType.SELECT).build();
            var boundSql = new BoundSql(configuration, sql, List.of(), null);
            interceptor.beforeQuery(null, statement, null, RowBounds.DEFAULT, null, boundSql);
            assertTrue(boundSql.getSql().contains("department_id IN 7"), boundSql.getSql());
        }
    }

    @Test
    void rejectsUnavailableScopeInsteadOfGrantingAll() {
        var client = mock(SysBasicSupportFeignClient.class);
        when(client.dataScope(42L)).thenReturn(R.ok((SysDataScopeDTO) null));
        UserContext.set(42L);
        TenantContext.set(1L);
        try (var context = context(client)) {
            assertThrows(RuntimeException.class, () -> context.getBean(DataScopeProvider.class).load(42L));
        }
    }

    @Test
    void rejectsScopeFromAnotherTenantOrUserAndUnknownScope() {
        var client = mock(SysBasicSupportFeignClient.class);
        UserContext.set(42L);
        TenantContext.set(1L);
        try (var context = context(client)) {
            var provider = context.getBean(DataScopeProvider.class);
            for (var dto : List.of(new SysDataScopeDTO(2L, 42L, "ALL", Set.of()),
                    new SysDataScopeDTO(1L, 99L, "ALL", Set.of()),
                    new SysDataScopeDTO(1L, 42L, "INVALID", Set.of()))) {
                when(client.dataScope(42L)).thenReturn(R.ok(dto));
                assertThrows(RuntimeException.class, () -> provider.load(42L));
            }
        }
    }

    @Test
    void reloadsScopeAfterTenantSwitchWithoutLocalUserOnlyCache() {
        var client = mock(SysBasicSupportFeignClient.class);
        when(client.dataScope(42L)).thenReturn(R.ok(new SysDataScopeDTO(1L, 42L, "ALL", Set.of())),
                R.ok(new SysDataScopeDTO(2L, 42L, "DEPT", Set.of(8L))));
        UserContext.set(42L);
        TenantContext.set(1L);
        try (var context = context(client)) {
            var provider = context.getBean(DataScopeProvider.class);
            assertEquals("ALL", provider.load(42L).getDataScope().getCode());
            assertEquals("ALL", DataPermissionHelper.getCurrentUserPermission().getDataScope().getCode());
            TenantContext.set(2L);
            assertEquals(Set.of(8L), provider.load(42L).getDeptIds());
        }
    }

    private AnnotationConfigApplicationContext context(SysBasicSupportFeignClient client) {
        var context = new AnnotationConfigApplicationContext();
        context.registerBean(SysBasicSupportFeignClient.class, () -> client);
        context.scan("com.forgex.basic.dataperm");
        context.refresh();
        return context;
    }

    @Test
    void registersProviderInBasicComponentScan() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(SysBasicSupportFeignClient.class, () -> mock(SysBasicSupportFeignClient.class));
            context.scan("com.forgex.basic.dataperm");
            context.refresh();
            assertEquals(1, context.getBeansOfType(DataScopeProvider.class).size(),
                    "Basic 的权限查询必须能加载 DataScopeProvider");
        }
    }
}

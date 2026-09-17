package com.forgex.common.crypto;

import com.forgex.common.config.ConfigService;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link FieldEncryptInterceptor} 在无加密字段时不得去读 {@code sys_config}。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FieldEncryptInterceptor
 */
class FieldEncryptInterceptorTest {

    /**
     * 菜单访问上报这类无 {@code @FieldEncrypt} 的 UPDATE 不应查询配置表。
     *
     * @throws Throwable 拦截器调用失败
     */
    @Test
    void updateWithoutEncryptFieldsDoesNotReadConfig() throws Throwable {
        ConfigService configService = mock(ConfigService.class);
        FieldEncryptInterceptor interceptor = new FieldEncryptInterceptor(configService);
        MappedStatement ms = mock(MappedStatement.class);
        when(ms.getSqlCommandType()).thenReturn(SqlCommandType.UPDATE);
        when(ms.getId()).thenReturn("com.forgex.sys.mapper.SysUserMenuOpenCountMapper.updateById");
        Invocation invocation = mock(Invocation.class);
        when(invocation.getArgs()).thenReturn(new Object[]{ms, new MenuOpenCount()});
        when(invocation.proceed()).thenReturn(1);

        interceptor.intercept(invocation);

        verify(configService, never()).getBoolean(anyString(), anyBoolean());
        verify(invocation).proceed();
    }

    /**
     * 参数扫描应识别带 {@code @FieldEncrypt} 的实体。
     */
    @Test
    void parameterScanDetectsEncryptFields() {
        FieldEncryptInterceptor interceptor = new FieldEncryptInterceptor(mock(ConfigService.class));
        assertFalse(interceptor.parameterHasEncryptFields(new MenuOpenCount()));
        EncryptedUser user = new EncryptedUser();
        user.phone = "13800000000";
        assertTrue(interceptor.parameterHasEncryptFields(user));
    }

    /**
     * 事务内读配置失败时回落默认开启，不把异常抛给业务 SQL。
     */
    @Test
    void enabledFlagFallsBackWhenConfigLookupFails() {
        ConfigService configService = mock(ConfigService.class);
        when(configService.getBoolean(anyString(), anyBoolean()))
                .thenThrow(new RuntimeException("Table 'forgex_admin.sys_config' doesn't exist"));
        FieldEncryptInterceptor interceptor = new FieldEncryptInterceptor(configService);
        assertTrue(interceptor.isFieldEncryptEnabled());
    }

    /**
     * 无加密字段的菜单打开次数实体，模拟首页访问上报。
     */
    private static class MenuOpenCount {
        private String menuTitle = "系统管理主页";
    }

    /**
     * 带字段加密注解的用户实体。
     */
    private static class EncryptedUser {
        @FieldEncrypt(algorithm = "sm4")
        private String phone;
    }
}

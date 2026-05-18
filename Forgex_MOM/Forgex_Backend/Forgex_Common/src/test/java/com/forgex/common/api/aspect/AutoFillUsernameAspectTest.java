package com.forgex.common.api.aspect;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.api.annotation.AutoFillUsername;
import com.forgex.common.api.feign.SysUserFeignClient;
import com.forgex.common.web.R;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 自动填充用户名切面测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-06
 */
class AutoFillUsernameAspectTest {

    @Test
    void shouldFillSingleObjectUsername() throws Throwable {
        SysUserFeignClient userFeignClient = mock(SysUserFeignClient.class);
        when(userFeignClient.getUsernameMap(anyList())).thenReturn(R.ok(Map.of(1L, "admin")));
        AutoFillUsernameAspect aspect = new AutoFillUsernameAspect(userFeignClient);
        AuditDTO dto = new AuditDTO("1");

        Object result = aspect.around(joinPointReturning(R.ok(dto)));

        assertEquals(R.class, result.getClass());
        assertEquals("admin", dto.getCreateByName());
        verify(userFeignClient, times(1)).getUsernameMap(List.of(1L));
    }

    @Test
    void shouldBatchFillCollectionUsernames() throws Throwable {
        SysUserFeignClient userFeignClient = mock(SysUserFeignClient.class);
        when(userFeignClient.getUsernameMap(anyList())).thenReturn(R.ok(Map.of(1L, "admin", 2L, "operator")));
        AutoFillUsernameAspect aspect = new AutoFillUsernameAspect(userFeignClient);
        AuditDTO first = new AuditDTO("1");
        AuditDTO second = new AuditDTO("2");

        aspect.around(joinPointReturning(R.ok(List.of(first, second))));

        assertEquals("admin", first.getCreateByName());
        assertEquals("operator", second.getCreateByName());
        verify(userFeignClient, times(1)).getUsernameMap(List.of(1L, 2L));
    }

    @Test
    void shouldBatchFillPageRecordUsernames() throws Throwable {
        SysUserFeignClient userFeignClient = mock(SysUserFeignClient.class);
        when(userFeignClient.getUsernameMap(anyList())).thenReturn(R.ok(Map.of(1L, "admin", 2L, "operator")));
        AutoFillUsernameAspect aspect = new AutoFillUsernameAspect(userFeignClient);
        AuditDTO first = new AuditDTO("1");
        AuditDTO second = new AuditDTO("2");
        Page<AuditDTO> page = new Page<>(1, 10);
        page.setRecords(List.of(first, second));

        aspect.around(joinPointReturning(R.ok(page)));

        assertEquals("admin", first.getCreateByName());
        assertEquals("operator", second.getCreateByName());
        verify(userFeignClient, times(1)).getUsernameMap(List.of(1L, 2L));
    }

    @Test
    void shouldResolveUsernameMapWithStringKeys() throws Throwable {
        SysUserFeignClient userFeignClient = mock(SysUserFeignClient.class);
        when(userFeignClient.getUsernameMap(anyList())).thenReturn(R.ok(Map.of(1L, "admin")));
        AutoFillUsernameAspect aspect = new AutoFillUsernameAspect(userFeignClient);
        StringKeyAuditDTO dto = new StringKeyAuditDTO("1");

        aspect.around(joinPointReturning(R.ok(dto)));

        assertEquals("admin", dto.getCreateByName());
    }

    @Test
    void shouldFillInheritedUserIdField() throws Throwable {
        SysUserFeignClient userFeignClient = mock(SysUserFeignClient.class);
        when(userFeignClient.getUsernameMap(anyList())).thenReturn(R.ok(Map.of(1L, "admin")));
        AutoFillUsernameAspect aspect = new AutoFillUsernameAspect(userFeignClient);
        InheritedAuditDTO dto = new InheritedAuditDTO("1");

        aspect.around(joinPointReturning(R.ok(dto)));

        assertEquals("admin", dto.getCreateByName());
    }

    @Test
    void shouldSkipJdkValueFieldsWhenFillingNestedBusinessObject() throws Throwable {
        SysUserFeignClient userFeignClient = mock(SysUserFeignClient.class);
        when(userFeignClient.getUsernameMap(anyList())).thenReturn(R.ok(Map.of(1L, "admin")));
        AutoFillUsernameAspect aspect = new AutoFillUsernameAspect(userFeignClient);
        WrapperDTO wrapper = new WrapperDTO(new AuditDTO("1"), Charset.defaultCharset());

        aspect.around(joinPointReturning(R.ok(wrapper)));

        assertEquals("admin", wrapper.getAudit().getCreateByName());
        verify(userFeignClient, times(1)).getUsernameMap(List.of(1L));
    }

    @Test
    void shouldSkipNonNumericOptionalUserIdWithoutQuerying() throws Throwable {
        SysUserFeignClient userFeignClient = mock(SysUserFeignClient.class);
        AutoFillUsernameAspect aspect = new AutoFillUsernameAspect(userFeignClient);
        AuditDTO dto = new AuditDTO("admin");

        aspect.around(joinPointReturning(R.ok(dto)));

        assertEquals(null, dto.getCreateByName());
        verify(userFeignClient, never()).getUsernameMap(anyList());
    }

    @Test
    void shouldSkipFrameworkReturnObject() throws Throwable {
        SysUserFeignClient userFeignClient = mock(SysUserFeignClient.class);
        AutoFillUsernameAspect aspect = new AutoFillUsernameAspect(userFeignClient);

        Object result = aspect.around(joinPointReturning(new SseEmitter(0L)));

        assertEquals(SseEmitter.class, result.getClass());
        verify(userFeignClient, never()).getUsernameMap(anyList());
    }

    private ProceedingJoinPoint joinPointReturning(Object result) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(result);
        return joinPoint;
    }

    @Data
    private static class AuditDTO {
        private String createBy;

        @AutoFillUsername(userIdField = "createBy")
        private String createByName;

        AuditDTO(String createBy) {
            this.createBy = createBy;
        }
    }

    @Data
    private static class BaseAuditDTO {
        private String createBy;

        BaseAuditDTO(String createBy) {
            this.createBy = createBy;
        }
    }

    @Data
    private static class InheritedAuditDTO extends BaseAuditDTO {
        @AutoFillUsername(userIdField = "createBy")
        private String createByName;

        InheritedAuditDTO(String createBy) {
            super(createBy);
        }
    }

    @Data
    private static class StringKeyAuditDTO {
        private String createBy;

        @AutoFillUsername(userIdField = "createBy")
        private String createByName;

        StringKeyAuditDTO(String createBy) {
            this.createBy = createBy;
        }
    }

    @Data
    private static class WrapperDTO {
        private final AuditDTO audit;
        private final Charset charset;
    }
}

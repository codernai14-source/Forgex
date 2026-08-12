package com.forgex.common.security.perm;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.i18n.I18nPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 接口权限拦截器（按钮级）。
 * <p>
 * 对标注了 {@link RequirePerm} 的接口进行强制鉴权：
 * - 依赖 {@link UserContext}/{@link TenantContext} 获取当前用户与租户；
 * - 依赖 {@link PermKeyService} 计算 permKey；
 * - 校验不通过抛出 {@link I18nBusinessException}，由全局异常处理器统一处理。
 * </p>
 *
 * <p>
 * 说明：Common 模块提供该拦截器，业务模块只需：
 * 1) 提供 {@link PermKeyService} Bean；
 * 2) 在本模块 WebMvc 配置中注册拦截路径（如 /sys/**）。
 * </p>
 *
 * <p>
 * 注意：该 Bean 仅在存在 {@link PermKeyService} 实现时才会创建，
 * 未提供实现的业务模块（如 Workflow）不会自动注册此拦截器。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see RequirePerm
 * @see PermKeyService
 */
@Component
@ConditionalOnBean(PermKeyService.class)
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";

    private final PermKeyService permKeyService;

    /**
     * 在 Controller 方法执行前进行权限校验。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @return 允许继续处理返回 true；拒绝返回 false
     * @throws I18nBusinessException 权限校验失败时抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws I18nBusinessException {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        Set<String> required = resolveRequiredPerms(hm);
        if (required.isEmpty()) {
            return true;
        }

        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();
        if (userId == null || tenantId == null) {
            Long headerUserId = parseLong(request == null ? null : request.getHeader(HEADER_USER_ID));
            Long headerTenantId = parseLong(request == null ? null : request.getHeader(HEADER_TENANT_ID));
            if (userId == null && headerUserId != null) {
                UserContext.set(headerUserId);
                userId = headerUserId;
            }
            if (tenantId == null && headerTenantId != null) {
                TenantContext.set(headerTenantId);
                tenantId = headerTenantId;
            }
        }
        if (userId == null || tenantId == null) {
            throw new I18nBusinessException(602, CommonPrompt.NOT_LOGIN);
        }

        boolean ok = permKeyService.hasAllPerms(userId, tenantId, required);
        if (!ok) {
            throw new I18nBusinessException(601, CommonPrompt.NO_PERMISSION);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
        TenantContext.clear();
    }

    /**
     * 解析 HandlerMethod 声明的权限键集合。
     *
     * @param hm HandlerMethod
     * @return 去重后的权限键集合
     */
    private Set<String> resolveRequiredPerms(HandlerMethod hm) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        RequirePerm classAnn = hm.getBeanType().getAnnotation(RequirePerm.class);
        if (classAnn != null && classAnn.value() != null) {
            set.addAll(Arrays.asList(classAnn.value()));
        }
        RequirePerm methodAnn = hm.getMethodAnnotation(RequirePerm.class);
        if (methodAnn != null && methodAnn.value() != null) {
            set.addAll(Arrays.asList(methodAnn.value()));
        }
        set.removeIf(s -> s == null || s.isBlank());
        return set;
    }

    /**
     * 解析长整型字符串。
     *
     * @param raw 字符串值
     * @return 长整型值，解析失败返回null
     */
    private Long parseLong(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return Long.valueOf(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }
}

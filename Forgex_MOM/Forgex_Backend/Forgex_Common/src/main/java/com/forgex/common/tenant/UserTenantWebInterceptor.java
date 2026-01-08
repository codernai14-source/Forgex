package com.forgex.common.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户与租户上下文注入拦截器。
 * <p>
 * 从 HTTP 请求头中读取由网关注入的用户与租户标识：
 * <ul>
 *     <li>X-User-Id：当前登录用户ID</li>
 *     <li>X-Tenant-Id：当前租户ID</li>
 * </ul>
 * 并将其写入 {@link UserContext} 与 {@link TenantContext}，在请求完成后统一清理，避免线程复用污染。
 *
 * @author Forgex
 * @version 1.0.0
 * @see UserContext
 * @see TenantContext
 */
public class UserTenantWebInterceptor implements HandlerInterceptor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";

    /**
     * 在请求处理前注入用户与租户上下文。
     *
     * @param request  当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler  将要执行的处理器
     * @return 一律返回 true 放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader(HEADER_USER_ID);
        if (StringUtils.hasText(userIdStr)) {
            try {
                Long uid = Long.valueOf(userIdStr);
                UserContext.set(uid);
            } catch (NumberFormatException ignored) {
            }
        }
        String tenantIdStr = request.getHeader(HEADER_TENANT_ID);
        if (StringUtils.hasText(tenantIdStr)) {
            try {
                Long tid = Long.valueOf(tenantIdStr);
                TenantContext.set(tid);
            } catch (NumberFormatException ignored) {
            }
        }
        return true;
    }

    /**
     * 在请求完成后清理用户与租户上下文。
     *
     * @param request  当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler  已执行的处理器
     * @param ex       处理过程中出现的异常（可能为 null）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
        TenantContext.clear();
    }
}


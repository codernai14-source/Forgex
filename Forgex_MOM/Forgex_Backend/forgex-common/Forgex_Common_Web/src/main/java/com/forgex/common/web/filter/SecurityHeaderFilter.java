package com.forgex.common.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 为 Servlet 响应补充基线安全响应头。
 * <p>
 * {@code X-Content-Type-Options: nosniff} 会禁止浏览器按内容猜测类型，
 * 因此公开文件接口必须返回正确的图片/视频 Content-Type，否则 Logo、背景、头像会裂图。
 * CSP 对 API JSON 本身不约束前端文档，但放行 {@code img-src}/{@code media-src}，
 * 避免响应头被反向代理挂到页面时拦截登录品牌资源和头像。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class SecurityHeaderFilter extends OncePerRequestFilter {

    /**
     * 基线 Content-Security-Policy。
     * <p>
     * 允许同源以及 http(s)、data、blob 图片和媒体，兼容历史绝对文件地址与登录背景视频。
     * </p>
     */
    static final String CONTENT_SECURITY_POLICY =
            "default-src 'self'; img-src 'self' data: blob: http: https:; "
                    + "media-src 'self' data: blob: http: https:; object-src 'none'; base-uri 'self'";

    /**
     * 写入安全响应头后继续过滤链。
     *
     * @param request     当前请求
     * @param response    当前响应
     * @param filterChain 过滤链
     * @throws ServletException 下游 Servlet 异常
     * @throws IOException      下游 IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy", CONTENT_SECURITY_POLICY);
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        filterChain.doFilter(request, response);
    }
}

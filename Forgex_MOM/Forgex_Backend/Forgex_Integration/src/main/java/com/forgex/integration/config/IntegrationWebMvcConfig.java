package com.forgex.integration.config;

import com.forgex.common.security.perm.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Integration 模块 WebMvc 配置。
 * <p>
 * 仅将集成平台后台管理接口纳入统一权限拦截，
 * 对外调用入口和第三方鉴权接口不在本次后台权限拦截范围内。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-17
 */
@Configuration
@RequiredArgsConstructor
public class IntegrationWebMvcConfig implements WebMvcConfigurer {

    private final PermissionInterceptor permissionInterceptor;

    /**
     * 注册集成平台后台管理接口的权限拦截器。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns(
                        "/api/integration/api-config/**",
                        "/api/integration/call-log/**",
                        "/api/integration/param-config/**",
                        "/api/integration/param-mapping/**",
                        "/api/integration/third-system/**"
                );
    }
}

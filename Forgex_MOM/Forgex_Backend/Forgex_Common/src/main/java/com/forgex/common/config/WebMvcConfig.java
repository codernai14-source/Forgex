package com.forgex.common.config;

import com.forgex.common.tenant.UserTenantWebInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * <p>注册用户与租户上下文拦截器，在所有请求进入业务处理前，统一从请求头中解析
 * 用户ID与租户ID，并写入 {@code UserContext} 与 {@code TenantContext}，在请求结束后
 * 自动清理，防止线程复用导致的上下文污染。</p>
 *
 * @author Forgex
 * @version 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册拦截器。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserTenantWebInterceptor()).addPathPatterns("/**");
    }
}

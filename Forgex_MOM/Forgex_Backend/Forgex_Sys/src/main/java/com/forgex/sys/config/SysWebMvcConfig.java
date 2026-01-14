package com.forgex.sys.config;

import com.forgex.common.security.perm.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sys 模块 WebMvc 配置。
 * <p>
 * 注册 {@link PermissionInterceptor}，对系统管理接口进行 permKey 强制鉴权。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see PermissionInterceptor
 */
@Configuration
@RequiredArgsConstructor
public class SysWebMvcConfig implements WebMvcConfigurer {

    private final PermissionInterceptor permissionInterceptor;

    /**
     * 注册拦截器。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor).addPathPatterns("/sys/**");
    }
}

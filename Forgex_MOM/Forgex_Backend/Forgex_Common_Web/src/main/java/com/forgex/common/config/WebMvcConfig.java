/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.common.config;

import com.forgex.common.i18n.LangWebInterceptor;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.tenant.UserTenantWebInterceptor;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.util.StringUtils;

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
        registry.addInterceptor(new LangWebInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(new UserTenantWebInterceptor()).addPathPatterns("/**");
    }

    /**
     * 注册语言上下文过滤器。
     * <p>
     * 兜底保证：即使在某些场景下 MVC 拦截器未生效，也能从请求头解析语言并写入 {@link LangContext}，
     * 避免菜单、字典、动态表格等依赖语言上下文的功能恒为默认语言。
     * </p>
     *
     * @return FilterRegistrationBean
     * @see LangContext
     * @see LangWebInterceptor
     */
    @Bean
    public FilterRegistrationBean<Filter> langContextFilter() {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
        bean.setFilter((ServletRequest request, ServletResponse response, FilterChain chain) -> {
            try {
                if (request instanceof HttpServletRequest httpReq) {
                    String lang = httpReq.getHeader(LangContext.HEADER_LANG);
                    if (!StringUtils.hasText(lang)) {
                        lang = httpReq.getHeader("Accept-Language");
                    }
                    LangContext.set(lang);
                } else {
                    LangContext.set(LangContext.DEFAULT_LANG);
                }
                chain.doFilter(request, response);
            } finally {
                LangContext.clear();
            }
        });
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}

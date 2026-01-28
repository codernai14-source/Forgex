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
package com.forgex.common.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 租户上下文清理拦截器。
 * <p>
 * 用于在请求结束后清理ThreadLocal中的租户上下文，防止内存泄漏。
 * </p>
 * <p>核心功能：</p>
 * <ul>
 *   <li>在请求完成后自动清理租户上下文</li>
 *   <li>防止ThreadLocal内存泄漏</li>
 *   <li>无需手动调用清理方法</li>
 * </ul>
 * <p>工作原理：</p>
 * <ol>
 *   <li>拦截器在请求处理完成后触发afterCompletion方法</li>
 *   <li>调用TenantContext.clear()清理当前线程的租户ID</li>
 *   <li>确保ThreadLocal资源被正确释放</li>
 * </ol>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 在WebMvcConfigurer中注册拦截器
 * @Configuration
 * public class WebMvcConfig implements WebMvcConfigurer {
 *     @Override
 *     public void addInterceptors(InterceptorRegistry registry) {
 *         registry.addInterceptor(new TenantContextInterceptor())
 *                 .addPathPatterns("/**");
 *     }
 * }
 * }</pre>
 * <p>注意事项：</p>
 * <ul>
 *   <li>必须注册到Spring MVC的拦截器链中</li>
 *   <li>需要配合TenantContext.set()方法使用</li>
 *   <li>拦截器顺序：应该在租户上下文设置拦截器之后执行</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see com.forgex.common.tenant.TenantContext
 * @see org.springframework.web.servlet.HandlerInterceptor
 */
public class TenantContextInterceptor implements HandlerInterceptor {
    
    /**
     * 请求处理完成后清理租户上下文。
     * <p>
     * 在整个请求处理完成后（包括视图渲染）调用此方法，清理ThreadLocal中的租户ID。
     * </p>
     * <p>清理时机：</p>
     * <ul>
     *   <li>请求正常处理完成</li>
     *   <li>请求抛出异常</li>
     *   <li>视图渲染完成</li>
     * </ul>
     * <p>清理流程：</p>
     * <ol>
     *   <li>调用TenantContext.clear()方法</li>
     *   <li>从当前线程的ThreadLocal中移除租户ID</li>
     *   <li>释放ThreadLocal资源，避免内存泄漏</li>
     * </ol>
     *
     * @param request  当前HTTP请求对象
     * @param response 当前HTTP响应对象
     * @param handler  被执行的处理器（Controller方法）
     * @param ex       处理过程中抛出的异常（如果有）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理当前线程的租户上下文，防止内存泄漏
        TenantContext.clear();
    }
}


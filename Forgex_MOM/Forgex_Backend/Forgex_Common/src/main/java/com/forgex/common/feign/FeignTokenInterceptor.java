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
package com.forgex.common.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求拦截器
 * <p>
 * 在 Feign 调用时自动传递当前请求的认证信息（Token）
 * 使得服务间调用无需重新登录
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2026-04-06
 */
public class FeignTokenInterceptor implements RequestInterceptor {

    /**
     * 拦截 Feign 请求，添加 Authorization Header
     *
     * @param template Feign 请求模板
     */
    @Override
    public void apply(RequestTemplate template) {
        // 从 RequestContextHolder 获取当前请求
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // 获取 Authorization Header
            String authorization = request.getHeader("Authorization");
            if (authorization != null && !authorization.isEmpty()) {
                template.header("Authorization", authorization);
            }
            
            // 获取 Tenant-Id Header（如果需要）
            String tenantId = request.getHeader("Tenant-Id");
            if (tenantId != null && !tenantId.isEmpty()) {
                template.header("Tenant-Id", tenantId);
            }
        }
    }
}

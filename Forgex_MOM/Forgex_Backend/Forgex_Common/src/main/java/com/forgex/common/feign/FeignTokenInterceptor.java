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
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Feign 请求拦截器。
 * <p>
 * 统一透传登录上下文，避免微服务间调用丢失身份信息。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.1.0
 * @date 2026-04-07
 */
public class FeignTokenInterceptor implements RequestInterceptor {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";
    private static final String HEADER_LEGACY_TENANT_ID = "Tenant-Id";

    /**
     * 拦截 Feign 请求并透传认证头。
     *
     * @param template Feign 请求模板
     */
    @Override
    public void apply(RequestTemplate template) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        String authorization = readHeader(attributes, HEADER_AUTHORIZATION);
        if (StringUtils.hasText(authorization)) {
            template.header(HEADER_AUTHORIZATION, authorization);
        }
        String userId = readHeader(attributes, HEADER_USER_ID);
        if (StringUtils.hasText(userId)) {
            template.header(HEADER_USER_ID, userId);
        }

        // 兼容读取旧头 Tenant-Id，统一写出 X-Tenant-Id。
        String tenantId = readHeader(attributes, HEADER_TENANT_ID);
        if (!StringUtils.hasText(tenantId)) {
            tenantId = readHeader(attributes, HEADER_LEGACY_TENANT_ID);
        }
        if (StringUtils.hasText(tenantId)) {
            template.header(HEADER_TENANT_ID, tenantId);
        }
    }

    /**
     * 通过反射读取 ServletRequestAttributes 的 request header。
     * 在 WebFlux 环境下无法解析 Servlet API 时返回 null，避免启动失败。
     */
    private String readHeader(RequestAttributes attributes, String headerName) {
        try {
            Object request = attributes.getClass().getMethod("getRequest").invoke(attributes);
            if (request == null) {
                return null;
            }
            Object value = request.getClass().getMethod("getHeader", String.class).invoke(request, headerName);
            return value == null ? null : String.valueOf(value);
        } catch (Exception ignore) {
            return null;
        }
    }
}

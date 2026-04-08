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
package com.forgex.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关租户上下文透传过滤器。
 * <p>
 * 统一从 Sa-Token 会话中提取当前用户和租户信息，并注入下游请求头。
 * </p>
 *
 * @author Forgex
 * @version 1.0.0
 */
@Component
public class TenantPropagationGlobalFilter implements GlobalFilter, Ordered {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";
    private static final String HEADER_ACCOUNT = "X-Account";
    private static final String HEADER_LANG = "X-Lang";

    private final StringRedisTemplate redis;
    private final GatewayLoginSessionSupport loginSessionSupport;

    public TenantPropagationGlobalFilter(
            StringRedisTemplate redis,
            GatewayLoginSessionSupport loginSessionSupport
    ) {
        this.redis = redis;
        this.loginSessionSupport = loginSessionSupport;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        GatewayLoginSessionSupport.LoginSessionContext loginContext = getLoginContext(exchange, request);
        if (loginContext == null) {
            return chain.filter(exchange);
        }

        String requestLang = request.getHeaders().getFirst(HEADER_LANG);
        if (!StringUtils.hasText(requestLang)) {
            requestLang = request.getHeaders().getFirst("Accept-Language");
        }

        ServerHttpRequest.Builder builder = request.mutate();
        if (loginContext.getUserId() != null) {
            builder.header(HEADER_USER_ID, String.valueOf(loginContext.getUserId()));
        }
        if (loginContext.getTenantId() != null) {
            builder.header(HEADER_TENANT_ID, String.valueOf(loginContext.getTenantId()));
        }
        if (StringUtils.hasText(loginContext.getAccount())) {
            builder.header(HEADER_ACCOUNT, loginContext.getAccount());
        }

        String resolvedLang = StringUtils.hasText(requestLang)
                ? requestLang
                : getUserLang(loginContext.getTenantId(), loginContext.getUserId());
        if (StringUtils.hasText(resolvedLang)) {
            builder.headers(headers -> headers.set(HEADER_LANG, resolvedLang));
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private GatewayLoginSessionSupport.LoginSessionContext getLoginContext(
            ServerWebExchange exchange,
            ServerHttpRequest request
    ) {
        Object cached = exchange.getAttribute(GatewayLoginSessionSupport.LOGIN_CONTEXT_ATTRIBUTE);
        if (cached instanceof GatewayLoginSessionSupport.LoginSessionContext context) {
            return context;
        }
        return loginSessionSupport.resolve(request);
    }

    private String getUserLang(Long tenantId, Long userId) {
        if (tenantId == null || userId == null) {
            return null;
        }
        try {
            return redis.opsForValue().get("fx:lang:" + tenantId + ":" + userId);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

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

import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;

/**
 * 网关层用户与租户上下文透传过滤器。
 * <p>
 * 从 Sa-Token 会话中读取当前登录用户与租户信息，并在转发请求前注入统一的 Header：
 * <ul>
 *     <li>X-User-Id：当前登录用户ID</li>
 *     <li>X-Tenant-Id：当前租户ID</li>
 *     <li>X-Account：当前登录账号</li>
 * </ul>
 * 该过滤器仅负责“读会话并注入Header”，不改变现有认证与鉴权逻辑，未登录场景保留原有行为。
 *
 * @author Forgex
 * @version 1.0.0
 * @see cn.dev33.satoken.stp.StpUtil
 * @see cn.dev33.satoken.session.SaSession
 */
@Component
public class TenantPropagationGlobalFilter implements GlobalFilter, Ordered {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";
    private static final String HEADER_ACCOUNT = "X-Account";
    private static final String HEADER_LANG = "X-Lang";

    private static final String COOKIE_TOKEN = "Authorization";

    @Autowired
    private StringRedisTemplate redis;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        
        // 获取请求路径
        String path = request.getURI().getPath();
        
        // 解析Token
        String token = resolveToken(request);
        
        // Token为空，直接放行
        if (!StringUtils.hasText(token)) {
            return chain.filter(exchange);
        }
        
        // 构建Redis键
        String key = "fx:login:ctx:" + token;
        
        // 从Redis查询用户登录上下文
        String json;
        try {
            json = redis.opsForValue().get(key);
        } catch (Exception e) {
            // Redis查询异常，直接放行
            return chain.filter(exchange);
        }
        
        // 登录上下文为空，直接放行
        if (!StringUtils.hasText(json)) {
            return chain.filter(exchange);
        }
        
        // 获取请求中的语言（优先自定义头，其次标准头）
        String requestLang = request.getHeaders().getFirst(HEADER_LANG);
        if (!StringUtils.hasText(requestLang)) {
            requestLang = request.getHeaders().getFirst("Accept-Language");
        }
        
        // 初始化变量
        Long userId = null;
        Long tenantId = null;
        String account = null;
        String lang = null;
        
        // 解析JSON，提取用户信息
        try {
            JSONObject obj = JSONUtil.parseObj(json);
            
            // 提取用户ID
            if (obj.containsKey("userId")) {
                userId = obj.getLong("userId");
            }
            
            // 提取租户ID
            if (obj.containsKey("tenantId")) {
                tenantId = obj.getLong("tenantId");
            }
            
            // 提取账号
            if (obj.containsKey("account")) {
                account = obj.getStr("account");
            }
            
            // 提取语言
            if (obj.containsKey("lang")) {
                lang = obj.getStr("lang");
            }
        } catch (Exception e) {
            // JSON解析异常，直接放行
            return chain.filter(exchange);
        }
        
        // 所有信息都为空，直接放行
        if (userId == null && tenantId == null && !StringUtils.hasText(account) && !StringUtils.hasText(lang)) {
            return chain.filter(exchange);
        }
        
        // 构建请求构建器
        ServerHttpRequest.Builder builder = request.mutate();
        
        // 注入用户ID Header
        if (userId != null) {
            builder.header(HEADER_USER_ID, String.valueOf(userId));
        }
        
        // 注入租户ID Header
        if (tenantId != null) {
            builder.header(HEADER_TENANT_ID, String.valueOf(tenantId));
        }
        
        // 注入账号 Header
        if (StringUtils.hasText(account)) {
            builder.header(HEADER_ACCOUNT, account);
        }
        
        // 如果请求中没有语言，尝试从Redis获取用户语言偏好
        if (!StringUtils.hasText(lang) && userId != null && tenantId != null) {
            try {
                lang = redis.opsForValue().get("fx:lang:" + tenantId + ":" + userId);
            } catch (Exception ignored) {}
        }
        
        // 解析最终语言（优先使用请求中的语言）
        String resolvedLang = StringUtils.hasText(requestLang) ? requestLang : lang;
        
        // 注入语言 Header
        if (StringUtils.hasText(resolvedLang)) {
            builder.headers(h -> h.set(HEADER_LANG, resolvedLang));
        }
        
        // 构建新请求
        ServerHttpRequest newRequest = builder.build();
        
        // 继续执行过滤器链
        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    /**
     * 从请求中解析 Sa-Token 的 token 值。
     * <p>
     * 优先从 Cookie 读取，其次从 Header 读取，均不存在时返回 null。
     *
     * @param request 当前请求对象
     * @return token 字符串，可能为 null
     */
    private String resolveToken(ServerHttpRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getCookies() != null) {
            HttpCookie cookie = request.getCookies().getFirst(COOKIE_TOKEN);
            if (cookie != null && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        HttpHeaders headers = request.getHeaders();
        if (headers != null) {
            String token = headers.getFirst(COOKIE_TOKEN);
            if (StringUtils.hasText(token)) {
                return token;
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

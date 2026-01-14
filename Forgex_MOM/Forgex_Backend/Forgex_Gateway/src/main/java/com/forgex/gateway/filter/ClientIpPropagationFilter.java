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


import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * 客户端真实 IP 透传过滤器
 * <p>
 * 从请求中提取客户端的真实 IP 地址，并通过 X-Client-IP 头透传给后端服务。
 * 提取顺序：
 * <ol>
 *     <li>X-Forwarded-For 头的第一个 IP（代理场景）</li>
 *     <li>X-Real-IP 头（Nginx 代理场景）</li>
 *     <li>RemoteAddress（直连场景）</li>
 * </ol>
 * </p>
 * 
 * @author coder_nai@163.com
 * @date 2025-01-13
 */
@Slf4j
@Component
public class ClientIpPropagationFilter implements GlobalFilter, Ordered {
    
    /**
     * 透传给后端的 Header 名称
     */
    private static final String HEADER_CLIENT_IP = "X-Client-IP";
    
    /**
     * X-Forwarded-For 头名称（标准代理头）
     */
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    
    /**
     * X-Real-IP 头名称（Nginx 代理头）
     */
    private static final String HEADER_X_REAL_IP = "X-Real-IP";
    
    /**
     * 未知 IP 标识
     */
    private static final String UNKNOWN_IP = "unknown";
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 提取真实 IP
        String clientIp = extractRealIp(request);
        
        // 添加到请求头
        ServerHttpRequest newRequest = request.mutate()
            .header(HEADER_CLIENT_IP, clientIp)
            .build();
        
        log.debug("客户端真实 IP: {}, 请求路径: {}", clientIp, request.getURI().getPath());
        
        // 继续处理
        return chain.filter(exchange.mutate().request(newRequest).build());
    }
    
    /**
     * 提取客户端真实 IP 地址
     * <p>
     * 按优先级依次尝试：
     * <ol>
     *     <li>X-Forwarded-For 头的第一个 IP</li>
     *     <li>X-Real-IP 头</li>
     *     <li>RemoteAddress</li>
     * </ol>
     * </p>
     * 
     * @param request 请求对象
     * @return 客户端真实 IP，提取失败返回 "unknown"
     */
    private String extractRealIp(ServerHttpRequest request) {
        if (request == null) {
            return UNKNOWN_IP;
        }
        
        HttpHeaders headers = request.getHeaders();
        
        // 1. 优先从 X-Forwarded-For 获取（代理场景）
        String xForwardedFor = headers.getFirst(HEADER_X_FORWARDED_FOR);
        if (StringUtils.hasText(xForwardedFor) && !UNKNOWN_IP.equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For 可能包含多个 IP，格式：client, proxy1, proxy2
            // 取第一个 IP 作为客户端真实 IP
            String[] ips = xForwardedFor.split(",");
            if (ips.length > 0) {
                String ip = ips[0].trim();
                if (StringUtils.hasText(ip) && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
                    log.debug("从 X-Forwarded-For 提取 IP: {}", ip);
                    return ip;
                }
            }
        }
        
        // 2. 其次从 X-Real-IP 获取（Nginx 代理场景）
        String xRealIp = headers.getFirst(HEADER_X_REAL_IP);
        if (StringUtils.hasText(xRealIp) && !UNKNOWN_IP.equalsIgnoreCase(xRealIp)) {
            log.debug("从 X-Real-IP 提取 IP: {}", xRealIp);
            return xRealIp;
        }
        
        // 3. 最后从 RemoteAddress 获取（直连场景）
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            String ip = remoteAddress.getAddress().getHostAddress();
            log.debug("从 RemoteAddress 提取 IP: {}", ip);
            return ip;
        }
        
        // 4. 都获取不到，返回 unknown
        log.warn("无法提取客户端真实 IP，请求路径: {}", request.getURI().getPath());
        return UNKNOWN_IP;
    }
    
    /**
     * 设置过滤器优先级
     * <p>
     * 返回 -200，确保在租户透传过滤器（-100）之前执行
     * </p>
     * 
     * @return 优先级值，数值越小优先级越高
     */
    @Override
    public int getOrder() {
        return -200;
    }
}

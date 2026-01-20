package com.forgex.gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 限流配置类
 * <p>配置网关限流策略，基于IP地址进行限流。</p>
 * <p><strong>IP获取优先级：</strong></p>
 * <ol>
 *   <li>X-Client-IP请求头</li>
 *   <li>X-Forwarded-For请求头（取第一个IP）</li>
 *   <li>远程地址</li>
 * </ol>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Configuration
public class RateLimitConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * 创建基于IP的限流Key解析器
     * <p>从请求中提取客户端IP地址作为限流Key。</p>
     * 
     * @return IP Key解析器
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getHeaders().getFirst("X-Client-IP");
            if (!StringUtils.hasText(ip)) {
                ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
                if (StringUtils.hasText(ip) && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
            }
            if (!StringUtils.hasText(ip) && exchange.getRequest().getRemoteAddress() != null) {
                ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }
            return Mono.just(StringUtils.hasText(ip) ? ip : "unknown");
        };
    }
}

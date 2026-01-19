package com.forgex.gateway.filter;

import cn.hutool.json.JSONUtil;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关认证全局过滤器
 * <p>负责对所有请求进行认证拦截，验证用户登录状态。</p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>拦截需要认证的请求路径</li>
 *   <li>从Cookie或Header中提取Token</li>
 *   <li>验证Token有效性（通过Redis查询）</li>
 *   <li>返回未登录响应</li>
 * </ul>
 * <p><strong>认证规则：</strong></p>
 * <ul>
 *   <li>OPTIONS请求直接放行</li>
 *   <li>/api/auth/路径下的请求直接放行</li>
 *   <li>/api/sys/和/api/files/路径下的请求需要认证</li>
 * </ul>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
public class GatewayAuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * Cookie中的Token名称
     */
    private static final String COOKIE_TOKEN = "satoken";

    /**
     * Redis模板，用于查询用户登录状态
     */
    @Autowired
    private StringRedisTemplate redis;

    /**
     * JSON对象映射器，用于序列化响应数据
     */
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        
        // 请求对象为空，直接放行
        if (request == null) {
            return chain.filter(exchange);
        }
        
        // 获取请求方法
        HttpMethod method = request.getMethod();
        
        // OPTIONS请求直接放行（CORS预检请求）
        if (method != null && HttpMethod.OPTIONS.matches(method.name())) {
            return chain.filter(exchange);
        }
        
        // 获取请求路径
        String path = request.getURI().getPath();
        
        // 不需要认证的路径，直接放行
        if (!needAuth(path)) {
            return chain.filter(exchange);
        }
        
        // 解析Token
        String token = resolveToken(request);
        
        // Token为空，返回未登录响应
        if (!StringUtils.hasText(token)) {
            return writeNotLogin(exchange);
        }
        
        // 构建Redis键
        String key = "fx:login:ctx:" + token;
        
        // 从Redis查询用户登录上下文
        String json;
        try {
            json = redis.opsForValue().get(key);
        } catch (Exception e) {
            // Redis查询异常，返回未登录响应
            return writeNotLogin(exchange);
        }
        
        // 登录上下文为空，返回未登录响应
        if (!StringUtils.hasText(json)) {
            return writeNotLogin(exchange);
        }
        
        // 验证JSON格式
        try {
            JSONUtil.parseObj(json);
        } catch (Exception ignored) {
            // JSON格式错误，返回未登录响应
            return writeNotLogin(exchange);
        }
        
        // 认证通过，继续执行过滤器链
        return chain.filter(exchange);
    }

    /**
     * 判断路径是否需要认证
     * <p>
     * /api/auth/路径下的请求直接放行，
     * /api/sys/和/api/files/路径下的请求需要认证。
     * </p>
     * 
     * @param path 请求路径
     * @return true=需要认证，false=不需要认证
     */
    private boolean needAuth(String path) {
        // 路径为空，不需要认证
        if (!StringUtils.hasText(path)) {
            return false;
        }
        
        // /api/auth/路径下的请求直接放行
        if (path.startsWith("/api/auth/")) {
            return false;
        }
        
        // /api/sys/和/api/files/路径下的请求需要认证
        return path.startsWith("/api/sys/") || path.startsWith("/api/files/");
    }

    /**
     * 从请求中解析Token
     * <p>
     * 优先从Cookie中读取，其次从Header中读取。
     * </p>
     * 
     * @param request HTTP请求对象
     * @return Token字符串，未找到返回null
     */
    private String resolveToken(ServerHttpRequest request) {
        // 从Cookie中读取Token
        if (request.getCookies() != null) {
            HttpCookie cookie = request.getCookies().getFirst(COOKIE_TOKEN);
            if (cookie != null && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        
        // 从Header中读取Token
        HttpHeaders headers = request.getHeaders();
        if (headers != null) {
            String token = headers.getFirst(COOKIE_TOKEN);
            if (StringUtils.hasText(token)) {
                return token;
            }
        }
        
        // 未找到Token
        return null;
    }

    /**
     * 写入未登录响应
     * <p>
     * 设置响应状态码为401，响应体为JSON格式的错误信息。
     * </p>
     * 
     * @param exchange 服务器Web交换对象
     * @return 响应写入完成后的Mono
     */
    private Mono<Void> writeNotLogin(ServerWebExchange exchange) {
        // 设置响应状态码为401未授权
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        
        // 设置响应内容类型为JSON
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // 构建响应体
        R<Object> body = R.fail(StatusCode.NOT_LOGIN, CommonPrompt.NOT_LOGIN);
        
        // 序列化响应体
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            // 序列化失败，使用默认错误消息
            bytes = "{\"code\":602,\"message\":\"NOT_LOGIN\"}".getBytes(StandardCharsets.UTF_8);
        }
        
        // 写入响应
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    /**
     * 获取过滤器执行顺序
     * <p>返回负数以确保在过滤器链中优先执行。</p>
     * 
     * @return 过滤器执行顺序值
     */
    @Override
    public int getOrder() {
        return -150;
    }
}

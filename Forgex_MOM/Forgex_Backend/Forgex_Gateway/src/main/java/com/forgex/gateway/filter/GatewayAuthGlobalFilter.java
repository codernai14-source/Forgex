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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Duration;

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
    private static final String COOKIE_TOKEN = "Authorization";

    private static final String SYS_I18N_RESOLVE_URL = "http://forgex-sys/sys/i18n/message/resolve";
    private static final String I18N_CACHE_PREFIX = "i18nmsg:";
    private static final Duration I18N_CACHE_TTL = Duration.ofHours(24);

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

    @Autowired
    private WebClient.Builder webClientBuilder;

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
        if (!needAuth(path, method)) {
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
    private boolean needAuth(String path, HttpMethod method) {
        // 路径为空，不需要认证
        if (!StringUtils.hasText(path)) {
            return false;
        }
        
        // /api/auth/路径下的请求直接放行
        if (path.startsWith("/api/auth/")) {
            return false;
        }

        // 登录页需要的公开接口直接放行
        if (path.equals("/api/sys/config/system-basic") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/config/login-captcha") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/i18n/languageType/listEnabled") && HttpMethod.POST.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/init/status") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/init/apply") && HttpMethod.POST.equals(method)) {
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
     * 设置响应体为JSON格式的错误信息。
     * </p>
     * 
     * @param exchange 服务器Web交换对象
     * @return 响应写入完成后的Mono
     */
    private Mono<Void> writeNotLogin(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        
        // 设置响应内容类型为JSON
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // 构建响应体
        R<Object> body = R.fail(StatusCode.NOT_LOGIN, CommonPrompt.NOT_LOGIN);
        HttpHeaders headers = exchange == null || exchange.getRequest() == null ? null : exchange.getRequest().getHeaders();

        return translate(CommonPrompt.NOT_LOGIN, body.getI18n() == null ? null : body.getI18n().getArgs(), headers)
                .defaultIfEmpty(CommonPrompt.NOT_LOGIN.getDefaultTemplate())
                .flatMap(msg -> {
                    body.setMessage(msg);

                    byte[] bytes;
                    try {
                        bytes = objectMapper.writeValueAsBytes(body);
                    } catch (Exception e) {
                        bytes = "{\"code\":602,\"message\":\"NOT_LOGIN\"}".getBytes(StandardCharsets.UTF_8);
                    }

                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
                });
    }

    private Mono<String> translate(CommonPrompt prompt, Object[] args, HttpHeaders headers) {
        if (prompt == null) {
            return Mono.empty();
        }

        String lang = normalizeLang(resolveLang(headers));
        String cacheKey = buildI18nCacheKey(lang, prompt);
        String cachedTemplate = null;
        try {
            cachedTemplate = redis.opsForValue().get(cacheKey);
        } catch (Exception ignored) {
        }
        if (StringUtils.hasText(cachedTemplate)) {
            return Mono.just(formatTemplate(cachedTemplate, args));
        }

        ResolveRequest req = new ResolveRequest();
        req.module = prompt.getModule();
        req.code = prompt.getPromptCode();
        req.defaultTemplate = prompt.getDefaultTemplate();

        return webClientBuilder.build()
                .post()
                .uri(SYS_I18N_RESOLVE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Lang", StringUtils.hasText(lang) ? lang : "")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(RString.class)
                .map(r -> r == null ? null : r.data)
                .timeout(Duration.ofSeconds(2))
                .onErrorReturn(null)
                .map(tpl -> {
                    if (!StringUtils.hasText(tpl)) {
                        return fallbackTranslate(prompt, args, lang);
                    }
                    try {
                        redis.opsForValue().set(cacheKey, tpl, I18N_CACHE_TTL);
                    } catch (Exception ignored) {
                    }
                    return formatTemplate(tpl, args);
                });
    }

    private String resolveLang(HttpHeaders headers) {
        if (headers == null) {
            return null;
        }
        String lang = headers.getFirst("X-Lang");
        if (!StringUtils.hasText(lang)) {
            lang = headers.getFirst("Accept-Language");
        }
        if (!StringUtils.hasText(lang)) {
            return null;
        }
        String v = lang.trim();
        int comma = v.indexOf(',');
        if (comma >= 0) {
            v = v.substring(0, comma).trim();
        }
        int semi = v.indexOf(';');
        if (semi >= 0) {
            v = v.substring(0, semi).trim();
        }
        return v;
    }

    private String formatTemplate(String template, Object[] args) {
        if (!StringUtils.hasText(template)) {
            return null;
        }
        if (args == null || args.length == 0) {
            return template;
        }
        try {
            return MessageFormat.format(template, args);
        } catch (Exception e) {
            String out = template;
            for (int i = 0; i < args.length; i++) {
                out = out.replace("{" + i + "}", String.valueOf(args[i]));
            }
            return out;
        }
    }

    private String fallbackTranslate(CommonPrompt prompt, Object[] args, String lang) {
        boolean english = StringUtils.hasText(lang) && lang.toLowerCase().startsWith("en");
        if (english) {
            String tpl = switch (prompt) {
                case NOT_LOGIN -> "Not logged in or session expired";
                case NO_PERMISSION -> "No permission";
                case INTERFACE_NOT_FOUND -> "Interface not found";
                case GATEWAY_ERROR -> "Gateway error: {0}";
                case MODULE_OFFLINE -> "{0} service is not running";
                case INTERNAL_SERVER_ERROR_MSG, INTERNAL_SERVER_ERROR -> "Internal server error: {0}";
                case BAD_REQUEST -> "Bad request: {0}";
                default -> null;
            };
            if (StringUtils.hasText(tpl)) {
                return formatTemplate(tpl, args);
            }
        }
        return formatTemplate(prompt.getDefaultTemplate(), args);
    }

    private String normalizeLang(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String v = raw.trim();
        if (v.equalsIgnoreCase("zh") || v.equalsIgnoreCase("zh-cn")) {
            return "zh-CN";
        }
        if (v.equalsIgnoreCase("en") || v.equalsIgnoreCase("en-us")) {
            return "en-US";
        }
        return v;
    }

    private String buildI18nCacheKey(String lang, CommonPrompt prompt) {
        String langKey = StringUtils.hasText(lang) ? lang : "zh-CN";
        return I18N_CACHE_PREFIX + langKey + ":" + prompt.getModule() + ":" + prompt.getPromptCode();
    }

    private static class ResolveRequest {
        public String module;
        public String code;
        public String defaultTemplate;
    }

    private static class RString {
        public Integer code;
        public String message;
        public String data;
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

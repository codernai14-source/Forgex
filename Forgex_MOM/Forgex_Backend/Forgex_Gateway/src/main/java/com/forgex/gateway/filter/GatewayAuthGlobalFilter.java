package com.forgex.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.license.LicenseManager;
import com.forgex.common.license.LicenseRuntimeInfo;
import com.forgex.common.license.LicenseStatus;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
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
 * 网关认证全局过滤器。
 * <p>
 * 统一基于 Sa-Token 会话判断请求是否已登录，不再依赖自建登录上下文缓存。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
public class GatewayAuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String SYS_I18N_RESOLVE_URL = "http://forgex-sys/sys/i18n/message/resolve";
    private static final String I18N_CACHE_PREFIX = "i18nmsg:";
    private static final Duration I18N_CACHE_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
    private final GatewayLoginSessionSupport loginSessionSupport;
    private final LicenseManager licenseManager;

    public GatewayAuthGlobalFilter(
            StringRedisTemplate redis,
            ObjectMapper objectMapper,
            WebClient.Builder webClientBuilder,
            GatewayLoginSessionSupport loginSessionSupport,
            LicenseManager licenseManager
    ) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.webClientBuilder = webClientBuilder;
        this.loginSessionSupport = loginSessionSupport;
        this.licenseManager = licenseManager;
    }

    /**
     * 过滤网关请求。
     *
     * @param exchange 汇率
     * @param chain chain
     * @return 处理结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (request == null) {
            return chain.filter(exchange);
        }

        HttpMethod method = request.getMethod();
        if (method != null && HttpMethod.OPTIONS.matches(method.name())) {
            return chain.filter(exchange);
        }

        String path = request.getURI().getPath();
        if (!needAuth(path, method)) {
            return chain.filter(exchange);
        }

        LicenseRuntimeInfo licenseRuntimeInfo = licenseManager.current();
        if (!licenseRuntimeInfo.isValid()) {
            return writeLicenseDenied(exchange, licenseRuntimeInfo);
        }

        GatewayLoginSessionSupport.LoginSessionContext loginContext = loginSessionSupport.resolve(request);
        if (loginContext == null) {
            return writeNotLogin(exchange);
        }

        exchange.getAttributes().put(GatewayLoginSessionSupport.LOGIN_CONTEXT_ATTRIBUTE, loginContext);
        loginSessionSupport.refreshOnlineUserTtl(loginContext);
        return chain.filter(exchange);
    }

    private boolean needAuth(String path, HttpMethod method) {
        if (!StringUtils.hasText(path)) {
            return false;
        }
        if (path.startsWith("/api/auth/")) {
            return false;
        }
        if (path.equals("/api/sys/config/system-basic") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/config/login-captcha") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/i18n/languageType/listEnabled") && HttpMethod.POST.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/i18n/languageType/getDefault") && HttpMethod.POST.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/i18n/message/mobileBundle") && HttpMethod.POST.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/init/status") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/init/apply") && HttpMethod.POST.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/license/status") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/license/request-info") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/license/logs") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/license/refresh") && HttpMethod.POST.equals(method)) {
            return false;
        }
        if (path.equals("/api/basic/module/ping") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/integration/invoke")) {
            return false;
        }
        if (path.equals("/api/integration/third-authorization/validate-token")) {
            return false;
        }
        if (path.startsWith("/api/integration/third-authorization/check-ip-whitelist/")) {
            return false;
        }
        if (path.startsWith("/api/files/") && (HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method))) {
            return false;
        }
        if (path.startsWith("/api/integration/api-config/")
                || path.startsWith("/api/integration/call-log/")
                || path.startsWith("/api/integration/param-config/")
                || path.startsWith("/api/integration/param-mapping/")
                || path.startsWith("/api/integration/third-system/")
                || path.startsWith("/api/integration/third-authorization/")) {
            return true;
        }
        return path.startsWith("/api/sys/")
                || path.startsWith("/api/basic/")
                || path.startsWith("/api/files/")
                || path.startsWith("/api/app/")
                || path.startsWith("/api/wf/")
                || path.startsWith("/api/report/");
    }

    private Mono<Void> writeNotLogin(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        R<Object> body = R.fail(StatusCode.NOT_LOGIN, CommonPrompt.NOT_LOGIN);
        HttpHeaders headers = exchange.getRequest() == null ? null : exchange.getRequest().getHeaders();

        return translate(CommonPrompt.NOT_LOGIN, body.getI18n() == null ? null : body.getI18n().getArgs(), headers)
                .defaultIfEmpty(CommonPrompt.NOT_LOGIN.getDefaultTemplate())
                .flatMap(message -> {
                    body.setMessage(message);

                    byte[] bytes;
                    try {
                        bytes = objectMapper.writeValueAsBytes(body);
                    } catch (Exception e) {
                        bytes = "{\"code\":602,\"message\":\"NOT_LOGIN\"}".getBytes(StandardCharsets.UTF_8);
                    }
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
                });
    }

    private Mono<Void> writeLicenseDenied(ServerWebExchange exchange, LicenseRuntimeInfo runtimeInfo) {
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        CommonPrompt prompt = switch (runtimeInfo.getStatus()) {
            case UNLICENSED -> CommonPrompt.LICENSE_REQUIRED;
            case EXPIRED -> CommonPrompt.LICENSE_EXPIRED;
            case GRACE -> CommonPrompt.LICENSE_GRACE;
            case INVALID -> CommonPrompt.LICENSE_INVALID;
            default -> CommonPrompt.LICENSE_INVALID;
        };

        Object[] args = prompt == CommonPrompt.LICENSE_INVALID ? new Object[]{runtimeInfo.getMessage()} : null;
        int code = runtimeInfo.getStatus() == LicenseStatus.UNLICENSED ? StatusCode.LICENSE_REQUIRED : StatusCode.LICENSE_INVALID;
        R<Object> body = args == null ? R.fail(code, prompt) : R.failWithArgs(code, prompt, args);
        HttpHeaders headers = exchange.getRequest() == null ? null : exchange.getRequest().getHeaders();

        return translate(prompt, body.getI18n() == null ? null : body.getI18n().getArgs(), headers)
                .defaultIfEmpty(prompt.getDefaultTemplate())
                .flatMap(message -> {
                    body.setMessage(message);

                    byte[] bytes;
                    try {
                        bytes = objectMapper.writeValueAsBytes(body);
                    } catch (Exception e) {
                        bytes = "{\"code\":605,\"message\":\"LICENSE_INVALID\"}".getBytes(StandardCharsets.UTF_8);
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
                .flatMap(r -> Mono.justOrEmpty(r == null ? null : r.data))
                .timeout(Duration.ofSeconds(2))
                .doOnNext(template -> {
                    if (!StringUtils.hasText(template)) {
                        return;
                    }
                    try {
                        redis.opsForValue().set(cacheKey, template, I18N_CACHE_TTL);
                    } catch (Exception ignored) {
                    }
                })
                .map(template -> formatTemplate(template, args))
                .onErrorResume(ignored -> Mono.empty())
                .switchIfEmpty(Mono.fromSupplier(() -> fallbackTranslate(prompt, args, lang)));
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
        String value = lang.trim();
        int comma = value.indexOf(',');
        if (comma >= 0) {
            value = value.substring(0, comma).trim();
        }
        int semi = value.indexOf(';');
        if (semi >= 0) {
            value = value.substring(0, semi).trim();
        }
        return value;
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
            String output = template;
            for (int i = 0; i < args.length; i++) {
                output = output.replace("{" + i + "}", String.valueOf(args[i]));
            }
            return output;
        }
    }

    private String fallbackTranslate(CommonPrompt prompt, Object[] args, String lang) {
        boolean english = StringUtils.hasText(lang) && lang.toLowerCase().startsWith("en");
        if (english) {
            String template = switch (prompt) {
                case NOT_LOGIN -> "Not logged in or session expired";
                case NO_PERMISSION -> "No permission";
                case INTERFACE_NOT_FOUND -> "Interface not found";
                case GATEWAY_ERROR -> "Gateway error: {0}";
                case MODULE_OFFLINE -> "{0} service is not running";
                case INTERNAL_SERVER_ERROR_MSG, INTERNAL_SERVER_ERROR -> "Internal server error: {0}";
                case BAD_REQUEST -> "Bad request: {0}";
                default -> null;
            };
            if (StringUtils.hasText(template)) {
                return formatTemplate(template, args);
            }
        }
        return formatTemplate(prompt.getDefaultTemplate(), args);
    }

    private String normalizeLang(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String value = raw.trim();
        if (value.equalsIgnoreCase("zh") || value.equalsIgnoreCase("zh-cn")) {
            return "zh-CN";
        }
        if (value.equalsIgnoreCase("en") || value.equalsIgnoreCase("en-us")) {
            return "en-US";
        }
        return value;
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
     * 获取order。
     *
     * @return 处理结果数量
     */
    @Override
    public int getOrder() {
        return -150;
    }
}

package com.forgex.gateway.web;

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.time.Duration;

/**
 * 网关全局异常处理器
 * <p>
 * 统一捕获网关层的异常并返回项目约定的业务错误结构
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestControllerAdvice
public class GatewayGlobalExceptionHandler {

    private static final String SYS_I18N_RESOLVE_URL = "http://forgex-sys/sys/i18n/message/resolve";
    private static final String I18N_CACHE_PREFIX = "i18nmsg:";
    private static final Duration I18N_CACHE_TTL = Duration.ofHours(24);

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * 处理服务未找到异常
     *
     * @param e 服务未找到异常
     * @return Mono包装的R对象
     */
    @ExceptionHandler(NotFoundException.class)
    public Mono<R<Object>> handleNotFound(NotFoundException e, ServerHttpRequest request) {
        String msg = e.getMessage();
        String name = extractServiceName(msg);
        String moduleName = name == null ? "目标" : name;
        R<Object> r = R.failWithArgs(StatusCode.MODULE_OFFLINE, CommonPrompt.MODULE_OFFLINE, new Object[]{moduleName});
        return translate(CommonPrompt.MODULE_OFFLINE, r.getI18n() == null ? null : r.getI18n().getArgs(),
                request == null ? null : request.getHeaders())
                .defaultIfEmpty(CommonPrompt.MODULE_OFFLINE.getDefaultTemplate())
                .map(msgText -> {
                    r.setMessage(msgText);
                    return r;
                });
    }

    /**
     * 处理响应状态异常
     *
     * @param e 响应状态异常
     * @return Mono包装的R对象
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<R<Object>> handleStatus(ResponseStatusException e, ServerHttpRequest request) {
        HttpHeaders headers = request == null ? null : request.getHeaders();
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            R<Object> r = R.fail(StatusCode.NOT_FOUND, CommonPrompt.INTERFACE_NOT_FOUND);
            return translate(CommonPrompt.INTERFACE_NOT_FOUND, r.getI18n() == null ? null : r.getI18n().getArgs(), headers)
                    .defaultIfEmpty(CommonPrompt.INTERFACE_NOT_FOUND.getDefaultTemplate())
                    .map(msgText -> {
                        r.setMessage(msgText);
                        return r;
                    });
        }
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            R<Object> r = R.fail(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION);
            return translate(CommonPrompt.NO_PERMISSION, r.getI18n() == null ? null : r.getI18n().getArgs(), headers)
                    .defaultIfEmpty(CommonPrompt.NO_PERMISSION.getDefaultTemplate())
                    .map(msgText -> {
                        r.setMessage(msgText);
                        return r;
                    });
        }
        if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            String msg = e.getReason() == null ? (e.getMessage() == null ? "" : e.getMessage()) : e.getReason();
            R<Object> r = R.failWithArgs(StatusCode.BUSINESS_ERROR, CommonPrompt.INTERNAL_SERVER_ERROR_MSG, new Object[]{msg});
            return translate(CommonPrompt.INTERNAL_SERVER_ERROR_MSG, r.getI18n() == null ? null : r.getI18n().getArgs(), headers)
                    .defaultIfEmpty(CommonPrompt.INTERNAL_SERVER_ERROR_MSG.getDefaultTemplate())
                    .map(msgText -> {
                        r.setMessage(msgText);
                        return r;
                    });
        }
        String msg = e.getReason() == null ? (e.getMessage() == null ? "" : e.getMessage()) : e.getReason();
        R<Object> r = R.failWithArgs(StatusCode.BUSINESS_ERROR, CommonPrompt.GATEWAY_ERROR, new Object[]{msg});
        return translate(CommonPrompt.GATEWAY_ERROR, r.getI18n() == null ? null : r.getI18n().getArgs(), headers)
                .defaultIfEmpty(CommonPrompt.GATEWAY_ERROR.getDefaultTemplate())
                .map(msgText -> {
                    r.setMessage(msgText);
                    return r;
                });
    }

    /**
     * 处理任意异常
     *
     * @param e 异常对象
     * @return Mono包装的R对象
     */
    @ExceptionHandler(Throwable.class)
    public Mono<R<Object>> handleAny(Throwable e, ServerHttpRequest request) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        HttpHeaders headers = request == null ? null : request.getHeaders();
        R<Object> r = R.failWithArgs(StatusCode.BUSINESS_ERROR, CommonPrompt.GATEWAY_ERROR, new Object[]{msg});
        return translate(CommonPrompt.GATEWAY_ERROR, r.getI18n() == null ? null : r.getI18n().getArgs(), headers)
                .defaultIfEmpty(CommonPrompt.GATEWAY_ERROR.getDefaultTemplate())
                .map(msgText -> {
                    r.setMessage(msgText);
                    return r;
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
                .doOnNext(tpl -> {
                    if (!StringUtils.hasText(tpl)) {
                        return;
                    }
                    try {
                        redis.opsForValue().set(cacheKey, tpl, I18N_CACHE_TTL);
                    } catch (Exception ignored) {
                    }
                })
                .map(tpl -> formatTemplate(tpl, args))
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
                case NO_PERMISSION -> "No permission";
                case INTERFACE_NOT_FOUND -> "Interface not found";
                case GATEWAY_ERROR -> "Gateway error: {0}";
                case MODULE_OFFLINE -> "{0} service is not running";
                case INTERNAL_SERVER_ERROR_MSG, INTERNAL_SERVER_ERROR -> "Internal server error: {0}";
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
     * 从异常消息中提取服务名称
     *
     * @param msg 异常消息
     * @return 服务名称
     */
    private String extractServiceName(String msg) {
        if (msg == null) return null;
        String k = "for ";
        int i = msg.lastIndexOf(k);
        if (i >= 0) {
            String s = msg.substring(i + k.length()).trim();
            int j = s.indexOf(' ');
            return j > 0 ? s.substring(0, j) : s;
        }
        return null;
    }
}

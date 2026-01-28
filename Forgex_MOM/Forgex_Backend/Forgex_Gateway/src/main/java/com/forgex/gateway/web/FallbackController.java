package com.forgex.gateway.web;

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.time.Duration;

/**
 * 网关降级控制器
 * <p>当后端服务不可用时，提供降级响应。</p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>拦截服务不可用请求</li>
 *   <li>返回友好的降级提示信息</li>
 * </ul>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
public class FallbackController {

    private static final String SYS_I18N_RESOLVE_URL = "http://forgex-sys/sys/i18n/message/resolve";
    private static final String I18N_CACHE_PREFIX = "i18nmsg:";
    private static final Duration I18N_CACHE_TTL = Duration.ofHours(24);

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * 服务降级处理
     * <p>当指定服务不可用时，返回降级响应。</p>
     * 
     * @param service 服务名称
     * @return 降级响应，包含服务不可用提示
     */
    @RequestMapping("/fallback/{service}")
    public Mono<R<Object>> fallback(@PathVariable("service") String service, ServerHttpRequest request) {
        String moduleName = service == null ? "目标" : service;
        R<Object> r = R.failWithArgs(StatusCode.MODULE_OFFLINE, CommonPrompt.MODULE_OFFLINE, new Object[]{moduleName});
        return translate(CommonPrompt.MODULE_OFFLINE, r.getI18n() == null ? null : r.getI18n().getArgs(),
                request == null ? null : request.getHeaders())
                .defaultIfEmpty(CommonPrompt.MODULE_OFFLINE.getDefaultTemplate())
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
                case MODULE_OFFLINE -> "{0} service is not running";
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
}

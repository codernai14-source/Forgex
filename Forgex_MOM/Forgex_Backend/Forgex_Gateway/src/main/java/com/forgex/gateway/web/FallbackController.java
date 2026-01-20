package com.forgex.gateway.web;

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

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
        R<Object> r = R.fail(StatusCode.MODULE_OFFLINE, CommonPrompt.MODULE_OFFLINE, moduleName);
        r.setMessage(translate(CommonPrompt.MODULE_OFFLINE, r.getI18n() == null ? null : r.getI18n().getArgs(),
                request == null ? null : request.getHeaders()));
        return Mono.just(r);
    }

    private String translate(CommonPrompt prompt, Object[] args, HttpHeaders headers) {
        if (prompt == null) {
            return null;
        }
        String lang = resolveLang(headers);
        boolean english = lang != null && lang.toLowerCase().startsWith("en");
        if (english) {
            String tpl = switch (prompt) {
                case MODULE_OFFLINE -> "{0} service is not running";
                default -> null;
            };
            if (StringUtils.hasText(tpl)) {
                return renderTemplate(tpl, args);
            }
        }
        return renderTemplate(prompt.getDefaultTemplate(), args);
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

    private String renderTemplate(String template, Object[] args) {
        if (!StringUtils.hasText(template)) {
            return null;
        }
        if (args == null || args.length == 0) {
            return template;
        }
        String out = template;
        for (int i = 0; i < args.length; i++) {
            out = out.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return out;
    }
}

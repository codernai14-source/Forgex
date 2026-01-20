package com.forgex.gateway.web;

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

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
        R<Object> r = R.fail(StatusCode.MODULE_OFFLINE, CommonPrompt.MODULE_OFFLINE, moduleName);
        r.setMessage(translate(CommonPrompt.MODULE_OFFLINE, r.getI18n() == null ? null : r.getI18n().getArgs(),
                request == null ? null : request.getHeaders()));
        return Mono.just(r);
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
            r.setMessage(translate(CommonPrompt.INTERFACE_NOT_FOUND, r.getI18n() == null ? null : r.getI18n().getArgs(), headers));
            return Mono.just(r);
        }
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            R<Object> r = R.fail(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION);
            r.setMessage(translate(CommonPrompt.NO_PERMISSION, r.getI18n() == null ? null : r.getI18n().getArgs(), headers));
            return Mono.just(r);
        }
        if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            String msg = e.getReason() == null ? (e.getMessage() == null ? "" : e.getMessage()) : e.getReason();
            R<Object> r = R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.INTERNAL_SERVER_ERROR_MSG, msg);
            r.setMessage(translate(CommonPrompt.INTERNAL_SERVER_ERROR_MSG, r.getI18n() == null ? null : r.getI18n().getArgs(), headers));
            return Mono.just(r);
        }
        String msg = e.getReason() == null ? (e.getMessage() == null ? "" : e.getMessage()) : e.getReason();
        R<Object> r = R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.GATEWAY_ERROR, msg);
        r.setMessage(translate(CommonPrompt.GATEWAY_ERROR, r.getI18n() == null ? null : r.getI18n().getArgs(), headers));
        return Mono.just(r);
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
        R<Object> r = R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.GATEWAY_ERROR, msg);
        r.setMessage(translate(CommonPrompt.GATEWAY_ERROR, r.getI18n() == null ? null : r.getI18n().getArgs(), headers));
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
                case NO_PERMISSION -> "No permission";
                case INTERFACE_NOT_FOUND -> "Interface not found";
                case GATEWAY_ERROR -> "Gateway error: {0}";
                case MODULE_OFFLINE -> "{0} service is not running";
                case INTERNAL_SERVER_ERROR_MSG, INTERNAL_SERVER_ERROR -> "Internal server error: {0}";
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

package com.forgex.gateway.web;

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.web.server.ResponseStatusException;
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
    public Mono<R<Object>> handleNotFound(NotFoundException e) {
        String msg = e.getMessage();
        String name = extractServiceName(msg);
        String moduleName = name == null ? "目标" : name;
        return Mono.just(R.fail(StatusCode.MODULE_OFFLINE, CommonPrompt.MODULE_OFFLINE, moduleName));
    }

    /**
     * 处理响应状态异常
     *
     * @param e 响应状态异常
     * @return Mono包装的R对象
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<R<Object>> handleStatus(ResponseStatusException e) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            return Mono.just(R.fail(StatusCode.NOT_FOUND, CommonPrompt.INTERFACE_NOT_FOUND));
        }
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return Mono.just(R.fail(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION));
        }
        if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            String msg = e.getReason() == null ? (e.getMessage() == null ? "" : e.getMessage()) : e.getReason();
            return Mono.just(R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.INTERNAL_SERVER_ERROR_MSG, msg));
        }
        String msg = e.getReason() == null ? (e.getMessage() == null ? "" : e.getMessage()) : e.getReason();
        return Mono.just(R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.GATEWAY_ERROR, msg));
    }

    /**
     * 处理任意异常
     *
     * @param e 异常对象
     * @return Mono包装的R对象
     */
    @ExceptionHandler(Throwable.class)
    public Mono<R<Object>> handleAny(Throwable e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        return Mono.just(R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.GATEWAY_ERROR, msg));
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

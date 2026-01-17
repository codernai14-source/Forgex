package com.forgex.common.web;

import com.forgex.common.i18n.I18nPrompt;
import com.forgex.common.service.i18n.I18nMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * R.message 国际化填充拦截器
 * <p>
 * 统一处理 {@link R} 返回体，将 {@link R#getMsg()}（枚举提示）或 {@link R#getI18n()}（元信息）
 * 解析为当前语言下的最终文案并写回 {@link R#setMessage(String)}。
 * </p>
 *
 * @author Forgex
 * @version 1.0.0
 * @see com.forgex.common.i18n.LangContext
 * @see I18nMessageService
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class RMessageI18nAdvice implements ResponseBodyAdvice<Object> {
    private final I18nMessageService i18nMessageService;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (body instanceof R<?> r) {
            I18nPrompt prompt = r.getMsg();
            if (prompt != null) {
                String resolved = i18nMessageService.resolve(prompt, r.getMsgArgs());
                if (resolved != null) {
                    r.setMessage(resolved);
                }
            } else if (r.getI18n() != null && !org.springframework.util.StringUtils.hasText(r.getMessage())) {
                String resolved = i18nMessageService.resolve(
                        r.getI18n().getModule(),
                        r.getI18n().getCode(),
                        r.getI18n().getArgs()
                );
                if (resolved != null) {
                    r.setMessage(resolved);
                }
            }
            r.setMsg(null);
            r.setMsgArgs(null);
        }
        return body;
    }
}

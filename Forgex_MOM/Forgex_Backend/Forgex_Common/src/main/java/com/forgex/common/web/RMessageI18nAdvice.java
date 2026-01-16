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
            }
            r.setMsg(null);
            r.setMsgArgs(null);
        }
        return body;
    }
}

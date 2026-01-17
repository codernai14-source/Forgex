package com.forgex.common.web;

import com.forgex.common.i18n.I18nPrompt;
import com.forgex.common.service.i18n.I18nMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * R.message 国际化填充拦截器
 * <p>
 * 统一处理 {@link R} 返回体，将 {@link R#getMessageCode()}（枚举提示）或 {@link R#getI18n()}（元信息）
 * 解析为当前语言下的最终文案并写回 {@link R#getMessage()}。
 * </p>
 * <p>解析规则：</p>
 * <ul>
 *   <li>message字段为占位符参数，按占位符顺序用逗号分隔</li>
 *   <li>通过messageCode + message参数组合，调用I18nMessageService进行国际化翻译</li>
 * </ul>
 *
 * @author Forgex Team
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
            I18nPrompt prompt = r.getMessageCode();
            String message = r.getMessage();

            if (prompt != null) {
                Object[] args = parseArgs(message);
                String resolved = i18nMessageService.resolve(prompt, args);
                if (resolved != null) {
                    r.setMessage(resolved);
                }
            } else if (r.getI18n() != null && !StringUtils.hasText(r.getMessage())) {
                String resolved = i18nMessageService.resolve(
                        r.getI18n().getModule(),
                        r.getI18n().getCode(),
                        r.getI18n().getArgs()
                );
                if (resolved != null) {
                    r.setMessage(resolved);
                }
            }
            r.setMessageCode(null);
        }
        return body;
    }

    /**
     * 解析占位符参数
     * <p>
     * 将逗号分隔的字符串解析为参数数组
     * </p>
     *
     * @param args 逗号分隔的参数字符串
     * @return 参数数组
     */
    private Object[] parseArgs(String args) {
        if (args == null || args.isEmpty()) {
            return null;
        }
        return args.split(",");
    }
}

package com.forgex.common.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

public class LangWebInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String lang = request.getHeader(LangContext.HEADER_LANG);
        if (!StringUtils.hasText(lang)) {
            lang = request.getHeader("Accept-Language");
        }
        if (StringUtils.hasText(lang)) {
            lang = normalize(lang);
            LangContext.set(lang);
        } else {
            LangContext.set(LangContext.DEFAULT_LANG);
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        LangContext.clear();
    }

    private String normalize(String raw) {
        String v = raw.trim();
        int comma = v.indexOf(',');
        if (comma > 0) {
            v = v.substring(0, comma);
        }
        int semi = v.indexOf(';');
        if (semi > 0) {
            v = v.substring(0, semi);
        }
        v = v.replace('_', '-');
        if (v.equalsIgnoreCase("zh") || v.equalsIgnoreCase("zh-cn")) {
            return "zh-CN";
        }
        if (v.equalsIgnoreCase("en") || v.equalsIgnoreCase("en-us")) {
            return "en-US";
        }
        return v;
    }
}


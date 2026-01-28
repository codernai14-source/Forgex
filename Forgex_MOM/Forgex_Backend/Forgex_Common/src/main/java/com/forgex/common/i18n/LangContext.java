package com.forgex.common.i18n;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class LangContext {
    public static final String HEADER_LANG = "X-Lang";
    public static final String DEFAULT_LANG = "zh-CN";

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private LangContext() {}

    public static void set(String lang) {
        if (!StringUtils.hasText(lang)) {
            HOLDER.set(DEFAULT_LANG);
            return;
        }
        HOLDER.set(normalize(lang));
    }

    public static String get() {
        String lang = HOLDER.get();
        if (StringUtils.hasText(lang)) {
            return lang;
        }

        String resolved = resolveFromRequest();
        if (StringUtils.hasText(resolved)) {
            HOLDER.set(resolved);
            return resolved;
        }

        return DEFAULT_LANG;
    }

    public static void clear() {
        HOLDER.remove();
    }

    private static String resolveFromRequest() {
        try {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            if (request == null) {
                return null;
            }

            String lang = request.getHeader(HEADER_LANG);
            if (!StringUtils.hasText(lang)) {
                lang = request.getHeader("Accept-Language");
            }
            if (!StringUtils.hasText(lang)) {
                return null;
            }
            return normalize(lang);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String normalize(String raw) {
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
        if (v.equalsIgnoreCase("zh-tw") || v.equalsIgnoreCase("zh-hk")) {
            return "zh-TW";
        }
        if (v.equalsIgnoreCase("en") || v.equalsIgnoreCase("en-us")) {
            return "en-US";
        }
        return v;
    }
}


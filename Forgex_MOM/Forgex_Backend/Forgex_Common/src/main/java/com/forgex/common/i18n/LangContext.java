package com.forgex.common.i18n;

public final class LangContext {
    public static final String HEADER_LANG = "X-Lang";
    public static final String DEFAULT_LANG = "zh-CN";

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private LangContext() {}

    public static void set(String lang) {
        HOLDER.set(lang);
    }

    public static String get() {
        String lang = HOLDER.get();
        return lang == null || lang.isBlank() ? DEFAULT_LANG : lang;
    }

    public static void clear() {
        HOLDER.remove();
    }
}


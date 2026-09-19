package com.forgex.common.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 语言上下文归一化测试。
 */
class LangContextTest {

    /**
     * 防止 ThreadLocal 在测试之间泄漏。
     */
    @AfterEach
    void clearContext() {
        LangContext.clear();
    }

    /**
     * 常用语言别名应归一为服务端约定语言标签。
     */
    @Test
    void normalizesCommonLanguageAliases() {
        LangContext.set("zh");
        assertEquals("zh-CN", LangContext.get());

        LangContext.set("zh-tw");
        assertEquals("zh-TW", LangContext.get());

        LangContext.set("en");
        assertEquals("en-US", LangContext.get());
    }
}

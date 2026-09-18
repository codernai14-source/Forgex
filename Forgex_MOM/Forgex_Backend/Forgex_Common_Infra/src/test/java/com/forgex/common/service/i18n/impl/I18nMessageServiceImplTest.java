package com.forgex.common.service.i18n.impl;

import com.forgex.common.domain.entity.i18n.FxI18nMessage;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.mapper.i18n.FxI18nMessageMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link I18nMessageServiceImpl} 的国际化模板解析测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class I18nMessageServiceImplTest {

    private final FxI18nMessageMapper mapper = mock(FxI18nMessageMapper.class);
    private final I18nMessageServiceImpl service = new I18nMessageServiceImpl(mapper);

    @AfterEach
    void clearLanguage() {
        LangContext.clear();
    }

    @Test
    void resolvesSelectedLanguageAndFormatsArguments() {
        FxI18nMessage row = new FxI18nMessage();
        row.setEnabled(true);
        row.setTextI18nJson("{\"zh-CN\":\"保存：{0}\",\"en-US\":\"Saved: {0}\"}");
        LangContext.set("en-US");
        when(mapper.selectOne(any())).thenReturn(row);

        assertEquals("Saved: profile", service.resolve(CommonPrompt.SAVE_SUCCESS, new Object[]{"profile"}));
    }

    @Test
    void removesUnresolvedPlaceholderAndTrailingSeparator() {
        FxI18nMessage row = new FxI18nMessage();
        row.setEnabled(true);
        row.setTextI18nJson("{\"zh-CN\":\"请求参数错误：{0}\"}");
        LangContext.set("zh-CN");
        when(mapper.selectOne(any())).thenReturn(row);

        assertEquals("请求参数错误", service.resolve(CommonPrompt.BAD_REQUEST, null));
    }

    @Test
    void fallsBackToDefaultTemplateWhenMapperFails() {
        LangContext.set("zh-CN");
        when(mapper.selectOne(any())).thenThrow(new IllegalStateException("database unavailable"));

        assertEquals("保存成功：用户", service.resolve(CommonPrompt.SAVE_SUCCESS, new Object[]{"用户"}));
    }
}

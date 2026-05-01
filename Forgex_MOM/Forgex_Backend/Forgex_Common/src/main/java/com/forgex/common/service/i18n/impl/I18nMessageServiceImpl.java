package com.forgex.common.service.i18n.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.domain.entity.i18n.FxI18nMessage;
import com.forgex.common.i18n.I18nPrompt;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.i18n.LegacyMessageTranslator;
import com.forgex.common.mapper.i18n.FxI18nMessageMapper;
import com.forgex.common.service.i18n.I18nMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;

/**
 * 国际化消息解析服务实现
 * <p>
 * 解析规则：
 * <ul>
 *     <li>优先根据 (module + promptCode) 从 {@code fx_i18n_message} 查询 JSON 文案</li>
 *     <li>按 {@link LangContext#get()} 选择对应语言（支持 en/zh 兜底）</li>
 *     <li>查不到时回退到默认模板（如提供），并在英文环境下做简单英译兜底</li>
 * </ul>
 * </p>
 *
 * @author Forgex
 * @version 1.0.0
 * @see com.forgex.common.i18n.I18nPrompt
 * @see com.forgex.common.i18n.LangContext
 */
@Service
@RequiredArgsConstructor
public class I18nMessageServiceImpl implements I18nMessageService {
    private final FxI18nMessageMapper i18nMessageMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String resolve(I18nPrompt prompt, Object[] args) {
        if (prompt == null) {
            return null;
        }
        String template = resolveTemplate(prompt.getModule(), prompt.getPromptCode(), prompt.getDefaultTemplate());
        if (!StringUtils.hasText(template)) {
            template = prompt.getDefaultTemplate();
        }
        if (!StringUtils.hasText(template)) {
            return null;
        }
        try {
            return MessageFormat.format(template, args == null ? new Object[0] : args);
        } catch (Exception e) {
            return template;
        }
    }

    @Override
    public String resolve(String module, String promptCode, Object[] args) {
        if (!StringUtils.hasText(module) || !StringUtils.hasText(promptCode)) {
            return null;
        }
        String template = resolveTemplate(module, promptCode, null);
        if (!StringUtils.hasText(template)) {
            return null;
        }
        try {
            return MessageFormat.format(template, args == null ? new Object[0] : args);
        } catch (Exception e) {
            return template;
        }
    }

    private String resolveTemplate(String module, String promptCode, String defaultTemplate) {
        FxI18nMessage row = i18nMessageMapper.selectOne(new LambdaQueryWrapper<FxI18nMessage>()
                .eq(FxI18nMessage::getModule, module)
                .eq(FxI18nMessage::getPromptCode, promptCode)
                .eq(FxI18nMessage::getDeleted, 0)
                .last("limit 1"));
        if (row == null || Boolean.FALSE.equals(row.getEnabled())) {
            // 如果没有找到翻译记录，直接返回对应语言的默认模板
            return getDefaultTemplateByLang(defaultTemplate);
        }
        String json = row.getTextI18nJson();
        if (!StringUtils.hasText(json)) {
            // 如果翻译JSON为空，直接返回对应语言的默认模板
            return getDefaultTemplateByLang(defaultTemplate);
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            String lang = LangContext.get();
            String val = getText(node, lang);
            if (StringUtils.hasText(val)) {
                return val;
            }
            if (StringUtils.hasText(lang)) {
                int idx = lang.indexOf('-');
                if (idx > 0) {
                    val = getText(node, lang.substring(0, idx));
                    if (StringUtils.hasText(val)) {
                        return val;
                    }
                }
            }
            
            val = getText(node, "zh-CN");
            if (StringUtils.hasText(val)) {
                return LegacyMessageTranslator.translate(val, lang);
            }
            val = getText(node, "zh");
            if (StringUtils.hasText(val)) {
                return val;
            }
            if (node.isObject()) {
                java.util.Iterator<java.util.Map.Entry<String, JsonNode>> it = node.fields();
                if (it.hasNext()) {
                    JsonNode first = it.next().getValue();
                    if (first != null && first.isTextual() && StringUtils.hasText(first.asText())) {
                        return first.asText();
                    }
                }
            }
            
            return getDefaultTemplateByLang(defaultTemplate);
        } catch (Exception e) {
            return getDefaultTemplateByLang(defaultTemplate);
        }
    }

    /**
     * 根据当前语言获取默认模板
     * 如果当前语言是英文，尝试翻译默认模板
     * @param defaultTemplate 默认模板
     * @return 对应语言的模板
     */
    private String getDefaultTemplateByLang(String defaultTemplate) {
        if (!StringUtils.hasText(defaultTemplate)) {
            return null;
        }
        String lang = LangContext.get();
        return LegacyMessageTranslator.translate(defaultTemplate, lang);
    }

    private String getText(JsonNode node, String key) {
        if (node == null || !node.isObject() || !StringUtils.hasText(key)) {
            return null;
        }
        JsonNode v = node.get(key);
        if (v != null && v.isTextual() && StringUtils.hasText(v.asText())) {
            return v.asText();
        }
        return null;
    }
}

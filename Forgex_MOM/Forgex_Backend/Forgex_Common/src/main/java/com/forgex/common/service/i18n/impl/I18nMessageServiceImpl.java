package com.forgex.common.service.i18n.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.domain.entity.i18n.FxI18nMessage;
import com.forgex.common.i18n.I18nPrompt;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.mapper.i18n.FxI18nMessageMapper;
import com.forgex.common.service.i18n.I18nMessageService;
import com.forgex.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;

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
        String template = resolveTemplate(prompt);
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

    private String resolveTemplate(I18nPrompt prompt) {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            return null;
        }
        FxI18nMessage row = i18nMessageMapper.selectOne(new LambdaQueryWrapper<FxI18nMessage>()
                .eq(FxI18nMessage::getTenantId, tenantId)
                .eq(FxI18nMessage::getModule, prompt.getModule())
                .eq(FxI18nMessage::getPromptCode, prompt.getPromptCode())
                .eq(FxI18nMessage::getDeleted, 0)
                .last("limit 1"));
        if (row == null || Boolean.FALSE.equals(row.getEnabled())) {
            return null;
        }
        String json = row.getTextI18nJson();
        if (!StringUtils.hasText(json)) {
            return null;
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
                return val;
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
            return null;
        } catch (Exception e) {
            return null;
        }
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


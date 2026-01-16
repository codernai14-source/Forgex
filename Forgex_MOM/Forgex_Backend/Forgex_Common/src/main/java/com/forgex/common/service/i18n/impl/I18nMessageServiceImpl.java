package com.forgex.common.service.i18n.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.domain.entity.i18n.FxI18nMessage;
import com.forgex.common.i18n.I18nPrompt;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.mapper.i18n.FxI18nMessageMapper;
import com.forgex.common.service.i18n.I18nMessageService;
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
        FxI18nMessage row = i18nMessageMapper.selectOne(new LambdaQueryWrapper<FxI18nMessage>()
                .eq(FxI18nMessage::getModule, prompt.getModule())
                .eq(FxI18nMessage::getPromptCode, prompt.getPromptCode())
                .eq(FxI18nMessage::getDeleted, 0)
                .last("limit 1"));
        if (row == null || Boolean.FALSE.equals(row.getEnabled())) {
            // 如果没有找到翻译记录，直接返回对应语言的默认模板
            return getDefaultTemplateByLang(prompt.getDefaultTemplate());
        }
        String json = row.getTextI18nJson();
        if (!StringUtils.hasText(json)) {
            // 如果翻译JSON为空，直接返回对应语言的默认模板
            return getDefaultTemplateByLang(prompt.getDefaultTemplate());
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
            
            // 如果当前语言是英文，尝试直接翻译默认模板
            if (lang.startsWith("en")) {
                return translateToEnglish(prompt.getDefaultTemplate());
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
            
            // 如果所有尝试都失败，返回对应语言的默认模板
            return getDefaultTemplateByLang(prompt.getDefaultTemplate());
        } catch (Exception e) {
            // 解析JSON失败时，返回对应语言的默认模板
            return getDefaultTemplateByLang(prompt.getDefaultTemplate());
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
        if (lang.startsWith("en")) {
            return translateToEnglish(defaultTemplate);
        }
        return defaultTemplate;
    }

    /**
     * 简单的中文到英文翻译，用于默认模板
     * 主要处理系统常见错误消息
     * @param chinese 中文文本
     * @return 英文翻译
     */
    private String translateToEnglish(String chinese) {
        if (!StringUtils.hasText(chinese)) {
            return chinese;
        }
        
        // 系统常见错误消息翻译
        switch (chinese) {
            case "表格配置{0}不存在":
                return "Table configuration {0} does not exist";
            case "表格{0}不允许通用查询":
                return "Table {0} does not allow general query";
            case "租户未选择":
                return "Tenant not selected";
            case "租户":
                return "Tenant";
            default:
                // 如果没有匹配的翻译，尝试简单替换
                return chinese
                        .replace("表格配置", "Table configuration")
                        .replace("不存在", "does not exist")
                        .replace("表格", "Table")
                        .replace("不允许", "does not allow")
                        .replace("通用查询", "general query")
                        .replace("租户", "Tenant")
                        .replace("未选择", "not selected");
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
package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.domain.entity.i18n.FxI18nMessage;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.mapper.i18n.FxI18nMessageMapper;
import com.forgex.common.web.R;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/i18n/message")
@RequiredArgsConstructor
public class I18nMessageController {

    private final FxI18nMessageMapper i18nMessageMapper;
    private final ObjectMapper objectMapper;

    @PostMapping("/resolve")
    public R<String> resolve(@RequestBody ResolveRequest body) {
        if (body == null || !StringUtils.hasText(body.getModule()) || !StringUtils.hasText(body.getCode())) {
            return R.ok("");
        }

        String resolved = resolveTemplate(body.getModule(), body.getCode(), body.getDefaultTemplate());
        return R.ok(StringUtils.hasText(resolved) ? resolved : "");
    }

    private String resolveTemplate(String module, String promptCode, String defaultTemplate) {
        FxI18nMessage row = i18nMessageMapper.selectOne(new LambdaQueryWrapper<FxI18nMessage>()
                .eq(FxI18nMessage::getModule, module)
                .eq(FxI18nMessage::getPromptCode, promptCode)
                .eq(FxI18nMessage::getDeleted, 0)
                .last("limit 1"));
        if (row == null || Boolean.FALSE.equals(row.getEnabled())) {
            return defaultTemplate;
        }

        String json = row.getTextI18nJson();
        if (!StringUtils.hasText(json)) {
            return defaultTemplate;
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
                while (it.hasNext()) {
                    JsonNode v = it.next().getValue();
                    if (v != null && v.isTextual() && StringUtils.hasText(v.asText())) {
                        return v.asText();
                    }
                }
            }

            return defaultTemplate;
        } catch (Exception e) {
            return defaultTemplate;
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

    @Data
    public static class ResolveRequest {
        private String module;
        private String code;
        private String defaultTemplate;
        private Object[] args;
    }
}


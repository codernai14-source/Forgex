package com.forgex.common.dict;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.domain.entity.dict.SysDictNode;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.mapper.dict.SysDictNodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DictI18nResolver {
    private final SysDictNodeMapper dictNodeMapper;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public Map<String, String> getChildLabelMap(Long tenantId, String nodePath) {
        if (tenantId == null || !StringUtils.hasText(nodePath)) {
            return Collections.emptyMap();
        }
        String lang = LangContext.get();
        String cacheKey = "dicti18n:" + tenantId + ":" + lang + ":" + nodePath;
        try {
            String raw = redis.opsForValue().get(cacheKey);
            if (StringUtils.hasText(raw)) {
                return objectMapper.readValue(raw, new TypeReference<Map<String, String>>() {});
            }
        } catch (Exception ignored) {}

        SysDictNode node = dictNodeMapper.selectOne(new LambdaQueryWrapper<SysDictNode>()
                .eq(SysDictNode::getDeleted, false)
                .eq(SysDictNode::getNodePath, nodePath)
                .last("limit 1"));
        if (node == null) {
            return Collections.emptyMap();
        }
        List<SysDictNode> children = dictNodeMapper.selectList(new LambdaQueryWrapper<SysDictNode>()
                .eq(SysDictNode::getDeleted, false)
                .eq(SysDictNode::getParentId, node.getId())
                .eq(SysDictNode::getStatus, 1)
                .orderByAsc(SysDictNode::getOrderNum));
        Map<String, String> map = new LinkedHashMap<>();
        for (SysDictNode c : children) {
            if (!StringUtils.hasText(c.getDictValue())) {
                continue;
            }
            map.put(c.getDictValue(), resolveI18nText(c.getDictValueI18nJson(), c.getDictName()));
        }
        try {
            String json = objectMapper.writeValueAsString(map);
            redis.opsForValue().set(cacheKey, json, 24, TimeUnit.HOURS);
        } catch (Exception ignored) {}
        return map;
    }

    private String resolveI18nText(String i18nJson, String fallback) {
        if (!StringUtils.hasText(i18nJson)) {
            return fallback;
        }
        try {
            JsonNode node = objectMapper.readTree(i18nJson);
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
                java.util.Iterator<Map.Entry<String, JsonNode>> it = node.fields();
                if (it.hasNext()) {
                    JsonNode first = it.next().getValue();
                    if (first != null && first.isTextual() && StringUtils.hasText(first.asText())) {
                        return first.asText();
                    }
                }
            }
            return fallback;
        } catch (Exception e) {
            return fallback;
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

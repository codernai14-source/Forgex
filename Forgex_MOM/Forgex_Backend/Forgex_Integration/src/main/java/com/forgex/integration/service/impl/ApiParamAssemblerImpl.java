package com.forgex.integration.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.integration.domain.dto.ApiParamMappingDTO;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.OutboundRequestDefinition;
import com.forgex.integration.enums.ApiMappingTargetScopeEnum;
import com.forgex.integration.enums.ApiMappingValueTypeEnum;
import com.forgex.integration.service.IApiParamAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 鍙傛暟缁勮鍣?
 */
@Service
@RequiredArgsConstructor
public class ApiParamAssemblerImpl implements IApiParamAssembler {

    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> assembleInbound(ApiDefinitionSnapshot snapshot, Map<String, Object> rawPayload) {
        Map<String, Object> source = rawPayload == null ? Map.of() : rawPayload;
        return applyMappings(snapshot.getInboundMappings(), source);
    }

    @Override
    public OutboundRequestDefinition assembleOutbound(ApiDefinitionSnapshot snapshot, Map<String, Object> rawPayload) {
        Map<String, Object> source = rawPayload == null ? Map.of() : rawPayload;
        Map<String, Object> body = new LinkedHashMap<>();
        Map<String, String> query = new LinkedHashMap<>();
        Map<String, String> headers = new LinkedHashMap<>();
        Map<String, String> pathVariables = new LinkedHashMap<>();
        List<ApiParamMappingDTO> mappings = snapshot.getOutboundMappings();
        if (mappings != null) {
            for (ApiParamMappingDTO mapping : mappings) {
                Object value = resolveValue(mapping, source);
                if (value == null) {
                    continue;
                }
                ApiMappingTargetScopeEnum scope = ApiMappingTargetScopeEnum.fromValue(mapping.getTargetScope());
                String target = mapping.getTargetFieldPath();
                switch (scope) {
                    case QUERY -> query.put(target, String.valueOf(value));
                    case HEADER -> headers.put(target, String.valueOf(value));
                    case PATH -> pathVariables.put(target, String.valueOf(value));
                    case BODY -> setNestedValue(body, target, value);
                }
            }
        }
        return OutboundRequestDefinition.builder()
            .body(body)
            .query(query)
            .headers(headers)
            .pathVariables(pathVariables)
            .build();
    }

    private Map<String, Object> applyMappings(List<ApiParamMappingDTO> mappings, Map<String, Object> source) {
        Map<String, Object> target = new LinkedHashMap<>();
        if (mappings == null || mappings.isEmpty()) {
            target.putAll(source);
            return target;
        }
        for (ApiParamMappingDTO mapping : mappings) {
            Object value = resolveValue(mapping, source);
            if (value == null) {
                continue;
            }
            setNestedValue(target, mapping.getTargetFieldPath(), value);
        }
        return target;
    }

    private Object resolveValue(ApiParamMappingDTO mapping, Map<String, Object> source) {
        ApiMappingValueTypeEnum valueType = ApiMappingValueTypeEnum.fromValue(mapping.getValueType());
        Object value = switch (valueType) {
            case CONSTANT -> mapping.getConstantValue();
            case DEFAULT -> mapping.getDefaultValue();
            case SOURCE -> getNestedValue(source, mapping.getSourceFieldPath());
        };
        if (value == null && StringUtils.hasText(mapping.getDefaultValue())) {
            value = mapping.getDefaultValue();
        }
        return applyTransform(mapping.getTransformRule(), value);
    }

    private Object applyTransform(String rule, Object value) {
        if (value == null || !StringUtils.hasText(rule)) {
            return value;
        }
        String normalized = rule.trim().toLowerCase(Locale.ROOT);
        if ("trim".equals(normalized)) {
            return String.valueOf(value).trim();
        }
        if ("uppercase".equals(normalized)) {
            return String.valueOf(value).toUpperCase(Locale.ROOT);
        }
        if ("lowercase".equals(normalized)) {
            return String.valueOf(value).toLowerCase(Locale.ROOT);
        }
        if ("number".equals(normalized)) {
            return Long.valueOf(String.valueOf(value));
        }
        if ("boolean".equals(normalized)) {
            return Boolean.valueOf(String.valueOf(value));
        }
        if (normalized.startsWith("date:")) {
            String pattern = normalized.substring("date:".length());
            String usePattern = StringUtils.hasText(pattern) ? pattern : DatePattern.NORM_DATETIME_PATTERN;
            return DateUtil.format(DateUtil.parse(String.valueOf(value)), usePattern);
        }
        return value;
    }

    private Object getNestedValue(Map<String, Object> source, String path) {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        Object current = source;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private void setNestedValue(Map<String, Object> target, String path, Object value) {
        if (!StringUtils.hasText(path)) {
            return;
        }
        String[] parts = path.split("\\.");
        Map<String, Object> current = target;
        for (int i = 0; i < parts.length - 1; i++) {
            Object child = current.get(parts[i]);
            if (!(child instanceof Map<?, ?>)) {
                child = new LinkedHashMap<String, Object>();
                current.put(parts[i], child);
            }
            current = (Map<String, Object>) child;
        }
        current.put(parts[parts.length - 1], value);
    }

    public Map<String, Object> toMap(Object entity) {
        if (entity == null) {
            return Map.of();
        }
        return objectMapper.convertValue(entity, new TypeReference<>() {});
    }
}

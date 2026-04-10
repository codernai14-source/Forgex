package com.forgex.common.service.excel.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.domain.dto.excel.TemplateOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 静态 JSON 选项提供者
 * <p>
 * 从配置的 JSON 字符串中解析选项列表。
 * 支持格式：[{"value": "0", "label": "停用"}, {"value": "1", "label": "启用"}]
 * 或简写格式：["0|停用", "1|启用"]
 * </p>
 *
 * @author Forgex Team
 * @version 1.1.0
 * @since 1.1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonOptionProvider implements TemplateOptionProvider {

    private final ObjectMapper objectMapper;

    /**
     * 获取 Provider 编码
     * <p>
     * 固定返回 "JSON"
     * </p>
     *
     * @return Provider 编码
     */
    @Override
    public String getCode() {
        return "JSON";
    }

    /**
     * 从 JSON 字符串解析选项列表
     * <p>
     * 支持两种格式：
     * 1. 对象数组：[{"value": "0", "label": "停用", "orderNum": 1}]
     * 2. 字符串数组：["0|停用", "1|启用"]
     * </p>
     *
     * @param context 上下文参数（未使用）
     * @param dataSourceValue JSON 字符串
     * @return 解析后的选项列表
     */
    @Override
    public List<TemplateOption> getOptions(TemplateOptionContext context, String dataSourceValue) {
        if (StringUtils.isBlank(dataSourceValue)) {
            log.warn("dataSourceValue is blank, return empty options");
            return new ArrayList<>();
        }
        
        log.debug("Parse JSON options, dataSourceValue={}", dataSourceValue);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(dataSourceValue);
            
            // 如果是数组
            if (jsonNode.isArray()) {
                List<TemplateOption> options = new ArrayList<>();
                
                // 判断是对象数组还是字符串数组
                JsonNode firstNode = jsonNode.iterator().hasNext() ? jsonNode.iterator().next() : null;
                
                if (firstNode != null && firstNode.isObject()) {
                    // 对象数组格式：[{"value": "0", "label": "停用", "orderNum": 1}]
                    for (JsonNode item : jsonNode) {
                        String value = item.has("value") ? item.get("value").asText() : "";
                        String label = item.has("label") ? item.get("label").asText() : value;
                        Integer orderNum = item.has("orderNum") ? item.get("orderNum").asInt() : 999;
                        String parentValue = item.has("parentValue") ? item.get("parentValue").asText() : null;
                        
                        TemplateOption option = new TemplateOption(value, label, orderNum);
                        option.setParentValue(parentValue);
                        options.add(option);
                    }
                } else if (firstNode != null && firstNode.isTextual()) {
                    // 字符串数组格式：["0|停用", "1|启用"]
                    for (JsonNode item : jsonNode) {
                        String text = item.asText();
                        String[] parts = text.split("\\|");
                        
                        String value = parts.length > 0 ? parts[0].trim() : text;
                        String label = parts.length > 1 ? parts[1].trim() : value;
                        
                        options.add(new TemplateOption(value, label));
                    }
                }
                
                return options;
            }
            
            log.warn("dataSourceValue is not a JSON array: {}", dataSourceValue);
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Parse JSON options failed, dataSourceValue={}", dataSourceValue, e);
            throw new IllegalArgumentException("解析 JSON 选项失败：" + e.getMessage(), e);
        }
    }
}

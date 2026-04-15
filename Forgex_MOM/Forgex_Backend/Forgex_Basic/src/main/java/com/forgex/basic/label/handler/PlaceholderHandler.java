package com.forgex.basic.label.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 占位符处理处理器
 * <p>
 * 负责将模板中的占位符替换为实际数据值，支持：
 * 1. 基础占位符替换：{{fieldName}}
 * 2. 带默认值的替换：{{fieldName:default}}
 * 3. 条件渲染：{{#if condition}}...{{/if}}
 * 4. 列表循环：{{#each items}}...{{/each}}
 * 5. 格式化指令：{{fieldName|uppercase}}、{{fieldName|date:yyyy-MM-dd}}
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Slf4j
@Component
public class PlaceholderHandler {

    /**
     * 基础占位符正则：{{fieldName}} 或 {{fieldName:defaultValue}}
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([a-zA-Z0-9_.]+)(?::([^}]*))?\\}\\}");

    /**
     * 条件渲染正则：{{#if condition}}...{{/if}}
     */
    private static final Pattern CONDITION_PATTERN = Pattern.compile("\\{\\{#if\\s+([a-zA-Z0-9_.]+)\\}\\}(.*?)\\{\\{/if\\}\\}", Pattern.DOTALL);

    /**
     * 循环渲染正则：{{#each listName}}...{{/each}}
     */
    private static final Pattern EACH_PATTERN = Pattern.compile("\\{\\{#each\\s+([a-zA-Z0-9_.]+)\\}\\}(.*?)\\{\\{/each\\}\\}", Pattern.DOTALL);

    /**
     * 格式化指令正则：{{fieldName|format}}
     */
    private static final Pattern FORMAT_PATTERN = Pattern.compile("\\{\\{([a-zA-Z0-9_.]+)\\|([a-zA-Z]+)(?::([^}]*))?\\}\\}");

    /**
     * 处理模板，替换所有占位符
     *
     * @param templateContent 模板内容
     * @param data 数据 Map
     * @return 替换后的内容
     */
    public String process(String templateContent, Map<String, Object> data) {
        if (templateContent == null || data == null) {
            return templateContent;
        }

        String result = templateContent;

        // 1. 处理条件渲染
        result = processConditions(result, data);

        // 2. 处理循环渲染
        result = processEach(result, data);

        // 3. 处理格式化指令
        result = processFormats(result, data);

        // 4. 处理基础占位符
        result = processBasicPlaceholders(result, data);

        log.debug("占位符处理完成");
        return result;
    }

    /**
     * 处理基础占位符
     *
     * @param content 内容
     * @param data 数据
     * @return 处理后的内容
     */
    private String processBasicPlaceholders(String content, Map<String, Object> data) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String defaultValue = matcher.group(2);

            Object value = getDataValue(data, fieldName);
            String replacement;

            if (value != null) {
                replacement = String.valueOf(value);
            } else if (defaultValue != null) {
                replacement = defaultValue;
            } else {
                replacement = "";
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 处理条件渲染
     * <p>
     * 示例：{{#if hasLogo}}<img src="logo.png"/>{{/if}}
     * </p>
     *
     * @param content 内容
     * @param data 数据
     * @return 处理后的内容
     */
    private String processConditions(String content, Map<String, Object> data) {
        Matcher matcher = CONDITION_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String conditionField = matcher.group(1);
            String innerContent = matcher.group(2);

            Object value = getDataValue(data, conditionField);
            boolean condition = isTruthy(value);

            String replacement = condition ? innerContent : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 处理循环渲染
     * <p>
     * 示例：{{#each items}}<div>{{name}}</div>{{/each}}
     * </p>
     *
     * @param content 内容
     * @param data 数据
     * @return 处理后的内容
     */
    private String processEach(String content, Map<String, Object> data) {
        Matcher matcher = EACH_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String listField = matcher.group(1);
            String template = matcher.group(2);

            Object listObj = getDataValue(data, listField);

            if (listObj instanceof List) {
                List<?> list = (List<?>) listObj;
                StringBuilder itemsHtml = new StringBuilder();

                for (Object item : list) {
                    String itemHtml = template;
                    if (item instanceof Map) {
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        // 递归处理每个项的占位符
                        itemHtml = processBasicPlaceholders(itemHtml, itemMap);
                    }
                    itemsHtml.append(itemHtml);
                }

                matcher.appendReplacement(sb, Matcher.quoteReplacement(itemsHtml.toString()));
            } else {
                matcher.appendReplacement(sb, "");
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 处理格式化指令
     * <p>
     * 示例：
     * - {{printDate|date:yyyy-MM-dd}}
     * - {{materialName|uppercase}}
     * - {{quantity|number:2}}
     * </p>
     *
     * @param content 内容
     * @param data 数据
     * @return 处理后的内容
     */
    private String processFormats(String content, Map<String, Object> data) {
        Matcher matcher = FORMAT_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String formatType = matcher.group(2);
            String formatParam = matcher.group(3);

            Object value = getDataValue(data, fieldName);
            String formatted = formatValue(value, formatType, formatParam);

            matcher.appendReplacement(sb, Matcher.quoteReplacement(formatted));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 从嵌套 Map 中获取值
     * <p>
     * 支持点号分隔的路径，如：material.name
     * </p>
     *
     * @param data 数据 Map
     * @param fieldPath 字段路径
     * @return 字段值
     */
    private Object getDataValue(Map<String, Object> data, String fieldPath) {
        if (!fieldPath.contains(".")) {
            return data.get(fieldPath);
        }

        String[] parts = fieldPath.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * 判断值是否为真
     *
     * @param value 值
     * @return true-真，false-假
     */
    private boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        return true;
    }

    /**
     * 格式化值
     *
     * @param value 原始值
     * @param formatType 格式化类型
     * @param formatParam 格式化参数
     * @return 格式化后的字符串
     */
    private String formatValue(Object value, String formatType, String formatParam) {
        if (value == null) {
            return "";
        }

        switch (formatType.toLowerCase()) {
            case "uppercase":
                return value.toString().toUpperCase();
            case "lowercase":
                return value.toString().toLowerCase();
            case "date":
                return formatDate(value, formatParam);
            case "number":
                return formatNumber(value, formatParam);
            default:
                return value.toString();
        }
    }

    /**
     * 格式化日期
     *
     * @param value 日期值
     * @param pattern 日期格式
     * @return 格式化后的日期字符串
     */
    private String formatDate(Object value, String pattern) {
        try {
            if (value instanceof LocalDateTime) {
                DateTimeFormatter formatter = pattern != null
                        ? DateTimeFormatter.ofPattern(pattern)
                        : DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return ((LocalDateTime) value).format(formatter);
            }
            return value.toString();
        } catch (Exception e) {
            log.warn("日期格式化失败: {}", value, e);
            return value.toString();
        }
    }

    /**
     * 格式化数字
     *
     * @param value 数字值
     * @param decimals 小数位数
     * @return 格式化后的数字字符串
     */
    private String formatNumber(Object value, String decimals) {
        try {
            if (value instanceof Number) {
                int decimalPlaces = decimals != null ? Integer.parseInt(decimals) : 2;
                return String.format("%." + decimalPlaces + "f", ((Number) value).doubleValue());
            }
            return value.toString();
        } catch (Exception e) {
            log.warn("数字格式化失败: {}", value, e);
            return value.toString();
        }
    }
}

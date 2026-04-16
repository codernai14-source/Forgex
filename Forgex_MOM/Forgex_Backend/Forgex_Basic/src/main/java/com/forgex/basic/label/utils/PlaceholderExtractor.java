package com.forgex.basic.label.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 占位符提取工具类
 * <p>
 * 用于从标签模板中提取、分析和验证占位符，支持：
 * 1. 提取所有占位符
 * 2. 去重和排序
 * 3. 验证数据完整性
 * 4. 占位符分组（按模块）
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Slf4j
public class PlaceholderExtractor {

    /**
     * 占位符正则表达式：{{fieldName}}
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([a-zA-Z0-9_.]+)}}");

    /**
     * 私有构造函数，防止实例化
     */
    private PlaceholderExtractor() {
    }

    /**
     * 提取模板中的所有占位符（不去重）
     *
     * @param templateContent 模板内容
     * @return 占位符列表（保持出现顺序）
     */
    public static List<String> extractAll(String templateContent) {
        List<String> placeholders = new ArrayList<>();
        if (templateContent == null || templateContent.isEmpty()) {
            return placeholders;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateContent);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }

        return placeholders;
    }

    /**
     * 提取模板中的所有占位符（去重）
     *
     * @param templateContent 模板内容
     * @return 去重后的占位符集合
     */
    public static Set<String> extractUnique(String templateContent) {
        return new LinkedHashSet<>(extractAll(templateContent));
    }

    /**
     * 提取并排序占位符
     *
     * @param templateContent 模板内容
     * @return 排序后的占位符列表
     */
    public static List<String> extractAndSort(String templateContent) {
        return extractUnique(templateContent).stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 统计占位符出现次数
     *
     * @param templateContent 模板内容
     * @return 占位符及其出现次数的映射
     */
    public static Map<String, Integer> countOccurrences(String templateContent) {
        Map<String, Integer> countMap = new HashMap<>();
        List<String> allPlaceholders = extractAll(templateContent);

        for (String placeholder : allPlaceholders) {
            countMap.merge(placeholder, 1, Integer::sum);
        }

        return countMap;
    }

    /**
     * 按前缀分组占位符
     * <p>
     * 例如：
     * - material.code, material.name -> material: [code, name]
     * - supplier.code -> supplier: [code]
     * </p>
     *
     * @param templateContent 模板内容
     * @return 分组后的占位符映射
     */
    public static Map<String, List<String>> groupByPrefix(String templateContent) {
        Set<String> uniquePlaceholders = extractUnique(templateContent);
        Map<String, List<String>> grouped = new TreeMap<>();

        for (String placeholder : uniquePlaceholders) {
            String[] parts = placeholder.split("\\.", 2);
            if (parts.length == 2) {
                String prefix = parts[0];
                String field = parts[1];
                grouped.computeIfAbsent(prefix, k -> new ArrayList<>()).add(field);
            } else {
                // 没有前缀的字段归到 "default" 组
                grouped.computeIfAbsent("default", k -> new ArrayList<>()).add(placeholder);
            }
        }

        return grouped;
    }

    /**
     * 验证打印数据是否包含所有必需的占位符
     *
     * @param templateContent 模板内容
     * @param printData 打印数据 Map
     * @return 缺失的占位符列表
     */
    public static List<String> validateDataCompleteness(String templateContent, Map<String, Object> printData) {
        Set<String> requiredPlaceholders = extractUnique(templateContent);
        List<String> missingFields = new ArrayList<>();

        for (String placeholder : requiredPlaceholders) {
            if (!printData.containsKey(placeholder)) {
                missingFields.add(placeholder);
            }
        }

        if (!missingFields.isEmpty()) {
            log.warn("打印数据缺少字段: {}", missingFields);
        }

        return missingFields;
    }

    /**
     * 检查模板是否包含指定占位符
     *
     * @param templateContent 模板内容
     * @param fieldName 字段名
     * @return true-包含，false-不包含
     */
    public static boolean containsPlaceholder(String templateContent, String fieldName) {
        if (templateContent == null || fieldName == null) {
            return false;
        }

        String pattern = "\\{\\{" + Pattern.quote(fieldName) + "}}";
        return Pattern.compile(pattern).matcher(templateContent).find();
    }

    /**
     * 获取占位符统计信息
     *
     * @param templateContent 模板内容
     * @return 统计信息字符串
     */
    public static String getStatistics(String templateContent) {
        List<String> all = extractAll(templateContent);
        Set<String> unique = extractUnique(templateContent);
        Map<String, Integer> counts = countOccurrences(templateContent);

        return String.format("占位符统计 - 总数: %d, 唯一: %d, 分布: %s",
                all.size(), unique.size(), counts);
    }

    /**
     * 替换模板中的占位符为实际值
     *
     * @param templateContent 模板内容
     * @param data 数据 Map
     * @return 替换后的内容
     */
    public static String replacePlaceholders(String templateContent, Map<String, Object> data) {
        if (templateContent == null || data == null) {
            return templateContent;
        }

        String result = templateContent;
        Set<String> placeholders = extractUnique(templateContent);

        for (String placeholder : placeholders) {
            Object value = data.get(placeholder);
            String replacement = value != null ? value.toString() : "";
            result = result.replace("{{" + placeholder + "}}", replacement);
        }

        return result;
    }
}

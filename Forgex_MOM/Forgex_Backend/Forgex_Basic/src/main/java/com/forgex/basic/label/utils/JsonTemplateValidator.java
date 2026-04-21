package com.forgex.basic.label.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON 模板校验器工具类
 * <p>
 * 用于验证标签模板的 JSON 内容是否合法，包括：
 * 1. JSON 格式校验
 * 2. 必填字段检查
 * 3. 占位符语法验证
 * 4. 模板结构完整性检查
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Slf4j
public class JsonTemplateValidator {

    /**
     * JSON 对象映射器
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 占位符正则表达式：{{fieldName}}
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([a-zA-Z0-9_.]+)}}");

    /**
     * 私有构造函数，防止实例化
     */
    private JsonTemplateValidator() {
    }

    /**
     * 校验模板 JSON 是否合法
     *
     * @param templateContent 模板 JSON 字符串
     * @return 校验结果
     */
    public static ValidationResult validate(String templateContent) {
        ValidationResult result = new ValidationResult();

        // 1. 检查是否为空
        if (templateContent == null || templateContent.trim().isEmpty()) {
            result.setValid(false);
            result.addError("模板内容不能为空");
            return result;
        }

        // 2. 校验 JSON 格式
        JsonNode jsonNode;
        try {
            jsonNode = OBJECT_MAPPER.readTree(templateContent);
        } catch (JsonProcessingException e) {
            result.setValid(false);
            result.addError("JSON 格式错误: " + e.getMessage());
            return result;
        }

        // 3. 校验必须是对象类型
        if (!jsonNode.isObject()) {
            result.setValid(false);
            result.addError("模板必须是 JSON 对象格式");
            return result;
        }

        // 4. 校验必填字段
        validateRequiredFields(jsonNode, result);

        // 5. 校验占位符语法
        validatePlaceholders(templateContent, result);

        // 6. 校验模板结构
        validateTemplateStructure(jsonNode, result);

        return result;
    }

    /**
     * 校验必填字段
     *
     * @param jsonNode JSON 节点
     * @param result 校验结果
     */
    private static void validateRequiredFields(JsonNode jsonNode, ValidationResult result) {
        // 检查是否有 layout 或 elements 字段（根据实际模板结构设计）
        if (!jsonNode.has("layout") && !jsonNode.has("elements")) {
            result.addWarning("模板缺少 layout 或 elements 字段，可能导致渲染异常");
        }

        // 检查是否有 version 字段
        if (!jsonNode.has("version")) {
            result.addWarning("模板缺少 version 字段，建议添加版本信息");
        }
    }

    /**
     * 校验占位符语法
     *
     * @param templateContent 模板内容
     * @param result 校验结果
     */
    private static void validatePlaceholders(String templateContent, ValidationResult result) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateContent);
        List<String> placeholders = new ArrayList<>();

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            placeholders.add(placeholder);

            // 检查占位符命名规范（只能包含字母、数字、下划线、点号）
            if (!placeholder.matches("[a-zA-Z0-9_.]+")) {
                result.addError("占位符格式不规范: {{" + placeholder + "}}");
            }
        }

        // 记录找到的占位符（用于后续验证）
        result.setPlaceholders(placeholders);

        if (placeholders.isEmpty()) {
            result.addWarning("模板中未找到任何占位符，可能不需要动态数据");
        } else {
            log.debug("找到 {} 个占位符: {}", placeholders.size(), placeholders);
        }
    }

    /**
     * 校验模板结构
     *
     * @param jsonNode JSON 节点
     * @param result 校验结果
     */
    private static void validateTemplateStructure(JsonNode jsonNode, ValidationResult result) {
        // 检查是否有 width 和 height（如果有的话，必须是数字）
        if (jsonNode.has("width")) {
            JsonNode widthNode = jsonNode.get("width");
            if (!widthNode.isNumber()) {
                result.addError("width 字段必须是数字类型");
            }
        }

        if (jsonNode.has("height")) {
            JsonNode heightNode = jsonNode.get("height");
            if (!heightNode.isNumber()) {
                result.addError("height 字段必须是数字类型");
            }
        }

        // 检查 elements 数组（如果存在）
        if (jsonNode.has("elements")) {
            JsonNode elementsNode = jsonNode.get("elements");
            if (!elementsNode.isArray()) {
                result.addError("elements 字段必须是数组类型");
            } else {
                // 校验每个元素
                for (JsonNode element : elementsNode) {
                    if (!element.has("type")) {
                        result.addError("elements 中的元素缺少 type 字段");
                    }
                }
            }
        }
    }

    /**
     * 快速校验 JSON 格式（仅检查格式，不检查业务逻辑）
     *
     * @param templateContent 模板内容
     * @return true-格式正确，false-格式错误
     */
    public static boolean isValidJson(String templateContent) {
        if (templateContent == null || templateContent.trim().isEmpty()) {
            return false;
        }

        try {
            OBJECT_MAPPER.readTree(templateContent);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 提取模板中的所有占位符
     *
     * @param templateContent 模板内容
     * @return 占位符列表
     */
    public static List<String> extractPlaceholders(String templateContent) {
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
     * 校验结果类
     */
    @Getter
    public static class ValidationResult {
        /**
         * 是否通过校验
         */
        @Setter
        private boolean valid = true;

        /**
         * 错误列表
         */
        private final List<String> errors = new ArrayList<>();

        /**
         * 警告列表
         */
        private final List<String> warnings = new ArrayList<>();

        /**
         * 占位符列表
         */
        @Setter
        private List<String> placeholders = new ArrayList<>();

        public void addError(String error) {
            this.errors.add(error);
            this.valid = false;
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationResult{valid=").append(valid);

            if (!errors.isEmpty()) {
                sb.append(", errors=").append(errors);
            }

            if (!warnings.isEmpty()) {
                sb.append(", warnings=").append(warnings);
            }

            if (!placeholders.isEmpty()) {
                sb.append(", placeholders=").append(placeholders);
            }

            sb.append('}');
            return sb.toString();
        }
    }
}


package com.forgex.common.dict;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典项
 * <p>
 * 封装字典项的完整信息，包括值、标签和样式。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code value} - 字典值</li>
 *   <li>{@code label} - 字典标签（国际化后的文本）</li>
 *   <li>{@code tagStyle} - 标签样式</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictItem {
    /**
     * 字典值
     */
    private String value;

    /**
     * 字典标签（国际化后的文本）
     */
    private String label;

    /**
     * 标签样式
     */
    private TagStyle tagStyle;

    /**
     * 转换为JSON字符串
     * <p>
     * 将字典项转换为JSON字符串，包含label和tagStyle信息。
     * </p>
     * 
     * @return JSON字符串
     */
    @JsonValue
    public String toJson() {
        if (tagStyle == null) {
            return label;
        }
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"label\":\"").append(label).append("\"");
        
        if (tagStyle.getColor() != null) {
            json.append(",\"color\":\"").append(tagStyle.getColor()).append("\"");
        }
        if (tagStyle.getBorderColor() != null) {
            json.append(",\"borderColor\":\"").append(tagStyle.getBorderColor()).append("\"");
        }
        if (tagStyle.getBackgroundColor() != null) {
            json.append(",\"backgroundColor\":\"").append(tagStyle.getBackgroundColor()).append("\"");
        }
        
        json.append("}");
        return json.toString();
    }

    /**
     * 标签样式
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagStyle {
        /**
         * 标签颜色
         */
        private String color;

        /**
         * 标签边框颜色
         */
        private String borderColor;

        /**
         * 标签背景色
         */
        private String backgroundColor;
    }
}

package com.forgex.integration.enums;

/**
 * 接口映射价值类型枚举。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public enum ApiMappingValueTypeEnum {
    SOURCE,
    DEFAULT,
    CONSTANT;

    /**
     * 处理fromvalue。
     *
     * @param value 价值
     * @return 处理结果
     */
    public static ApiMappingValueTypeEnum fromValue(String value) {
        if (value == null || value.isBlank()) {
            return SOURCE;
        }
        for (ApiMappingValueTypeEnum item : values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return SOURCE;
    }
}

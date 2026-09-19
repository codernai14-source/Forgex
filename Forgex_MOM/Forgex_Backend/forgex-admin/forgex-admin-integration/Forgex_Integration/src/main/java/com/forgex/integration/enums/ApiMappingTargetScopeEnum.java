package com.forgex.integration.enums;

/**
 * 接口映射目标scope枚举。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public enum ApiMappingTargetScopeEnum {
    BODY,
    QUERY,
    HEADER,
    PATH;

    /**
     * 处理fromvalue。
     *
     * @param value 价值
     * @return 处理结果
     */
    public static ApiMappingTargetScopeEnum fromValue(String value) {
        if (value == null || value.isBlank()) {
            return BODY;
        }
        for (ApiMappingTargetScopeEnum item : values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return BODY;
    }
}

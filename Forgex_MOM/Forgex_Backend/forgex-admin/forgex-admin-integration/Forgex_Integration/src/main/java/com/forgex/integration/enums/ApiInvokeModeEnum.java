package com.forgex.integration.enums;

/**
 * 接口调用模式枚举。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public enum ApiInvokeModeEnum {
    SYNC,
    ASYNC;

    /**
     * 处理fromvalue。
     *
     * @param value 价值
     * @return 处理结果
     */
    public static ApiInvokeModeEnum fromValue(String value) {
        if (value == null || value.isBlank()) {
            return SYNC;
        }
        for (ApiInvokeModeEnum item : values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return SYNC;
    }
}

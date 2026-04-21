package com.forgex.integration.enums;

public enum ApiInvokeModeEnum {
    SYNC,
    ASYNC;

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

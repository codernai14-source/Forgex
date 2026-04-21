package com.forgex.integration.enums;

public enum ApiMappingValueTypeEnum {
    SOURCE,
    DEFAULT,
    CONSTANT;

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

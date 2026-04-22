package com.forgex.integration.enums;

public enum ApiMappingTargetScopeEnum {
    BODY,
    QUERY,
    HEADER,
    PATH;

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

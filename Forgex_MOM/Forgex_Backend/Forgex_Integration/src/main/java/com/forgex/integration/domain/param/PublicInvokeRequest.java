package com.forgex.integration.domain.param;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 瀵瑰鍏叡璋冪敤鍏ュ弬
 */
@Data
public class PublicInvokeRequest {

    @JsonProperty("jiekou_code")
    private String interfaceCode;

    private final Map<String, Object> payload = new LinkedHashMap<>();

    @JsonAnySetter
    public void put(String key, Object value) {
        if (!"jiekou_code".equals(key)) {
            payload.put(key, value);
        }
    }
}

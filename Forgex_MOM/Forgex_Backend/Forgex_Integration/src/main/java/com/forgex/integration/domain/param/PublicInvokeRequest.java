package com.forgex.integration.domain.param;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对外公共调用入参。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class PublicInvokeRequest {

    /**
     * 接口编码。
     */
    @JsonProperty("jiekou_code")
    private String interfaceCode;

    private final Map<String, Object> payload = new LinkedHashMap<>();

    /**
     * 收集 jiekou_code 之外的请求字段。
     *
     * @param key 字段名
     * @param value 字段值
     */
    @JsonAnySetter
    public void put(String key, Object value) {
        if (!"jiekou_code".equals(key)) {
            payload.put(key, value);
        }
    }
}

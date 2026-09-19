package com.forgex.integration.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 鍑虹珯 HTTP 璇锋眰瀹氫箟
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@Builder
public class OutboundRequestDefinition {

    @Builder.Default
    private Map<String, Object> body = new LinkedHashMap<>();

    @Builder.Default
    private Map<String, String> query = new LinkedHashMap<>();

    @Builder.Default
    private Map<String, String> headers = new LinkedHashMap<>();

    @Builder.Default
    private Map<String, String> pathVariables = new LinkedHashMap<>();

    private Long outboundTargetId;

    private String targetSystemCode;

    private String targetSystemName;

    private String targetUrl;
}

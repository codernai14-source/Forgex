package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 客户第三方同步调用 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Data
public class CustomerThirdPartyInvokeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String apiCode;

    private Long tenantId;

    private Map<String, Object> payload = new LinkedHashMap<>();
}

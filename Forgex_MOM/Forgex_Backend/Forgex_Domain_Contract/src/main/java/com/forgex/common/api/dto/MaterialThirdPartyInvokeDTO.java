package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 物料第三方同步调用 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-12
 */
@Data
public class MaterialThirdPartyInvokeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接口编码。
     */
    private String apiCode;

    /**
     * 当前租户 ID。
     */
    private Long tenantId;

    /**
     * 调用载荷。
     */
    private Map<String, Object> payload = new LinkedHashMap<>();
}

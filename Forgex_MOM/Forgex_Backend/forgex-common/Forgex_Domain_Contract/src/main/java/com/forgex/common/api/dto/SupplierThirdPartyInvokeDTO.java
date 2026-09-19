package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 供应商第三方同步调用 DTO
 * <p>
 * 用于基础数据模块调用接口平台进行供应商出站或入站编排。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@Data
public class SupplierThirdPartyInvokeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接口编码
     */
    private String apiCode;

    /**
     * 当前租户 ID
     */
    private Long tenantId;

    /**
     * 调用载荷
     */
    private Map<String, Object> payload = new LinkedHashMap<>();
}

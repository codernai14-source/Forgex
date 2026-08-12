package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 人员第三方接口调用参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class EmployeeThirdPartyInvokeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 接口编码。 */
    private String apiCode;

    /** 租户 ID。 */
    private Long tenantId;

    /** 扩展载荷。 */
    private Map<String, Object> payload = new LinkedHashMap<>();
}

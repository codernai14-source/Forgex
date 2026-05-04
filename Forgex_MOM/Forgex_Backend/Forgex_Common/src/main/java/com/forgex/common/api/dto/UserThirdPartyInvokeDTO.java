package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户thirdparty调用数据传输对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class UserThirdPartyInvokeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String apiCode;

    private Long tenantId;

    private Map<String, Object> payload = new LinkedHashMap<>();
}

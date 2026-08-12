package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户第三方同步请求 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Data
public class CustomerThirdPartySyncRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private List<CustomerAggregateDTO> customers = new ArrayList<>();
}

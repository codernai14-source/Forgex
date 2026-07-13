package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户第三方同步结果 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Data
public class CustomerThirdPartySyncResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer totalCount = 0;

    private Integer createdCount = 0;

    private Integer updatedCount = 0;

    private Integer failedCount = 0;

    private List<String> failedCustomerCodes = new ArrayList<>();
}

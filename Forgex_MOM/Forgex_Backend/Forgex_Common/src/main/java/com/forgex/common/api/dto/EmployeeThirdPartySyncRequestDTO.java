package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 人员第三方同步请求。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class EmployeeThirdPartySyncRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 租户 ID。 */
    private Long tenantId;

    /** 人员列表。 */
    private List<EmployeeThirdPartySyncDTO> employees = new ArrayList<>();
}

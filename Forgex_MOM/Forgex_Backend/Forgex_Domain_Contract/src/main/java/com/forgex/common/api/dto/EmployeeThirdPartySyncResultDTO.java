package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 人员第三方同步结果。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class EmployeeThirdPartySyncResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总数。 */
    private Integer totalCount = 0;

    /** 新增数。 */
    private Integer createdCount = 0;

    /** 更新数。 */
    private Integer updatedCount = 0;

    /** 失败数。 */
    private Integer failedCount = 0;

    /** 失败工号。 */
    private List<String> failedEmployeeNos = new ArrayList<>();
}

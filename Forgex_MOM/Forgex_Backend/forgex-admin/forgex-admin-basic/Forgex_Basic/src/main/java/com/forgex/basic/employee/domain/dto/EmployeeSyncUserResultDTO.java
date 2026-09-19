package com.forgex.basic.employee.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 人员同步用户结果。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class EmployeeSyncUserResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总数。 */
    private Integer totalCount = 0;

    /** 新增用户数。 */
    private Integer createdCount = 0;

    /** 更新用户数。 */
    private Integer updatedCount = 0;

    /** 失败工号。 */
    private List<String> failedEmployeeNos = new ArrayList<>();
}

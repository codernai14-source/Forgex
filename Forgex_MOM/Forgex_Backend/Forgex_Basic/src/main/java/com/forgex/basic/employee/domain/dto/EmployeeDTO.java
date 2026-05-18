package com.forgex.basic.employee.domain.dto;

import com.forgex.basic.employee.domain.entity.BasicEmployee;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人员主数据 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeDTO extends BasicEmployee {

    /** 部门名称。 */
    private String departmentName;

    /** 岗位名称。 */
    private String positionName;

    /** 关联用户 ID。 */
    private Long userId;
}

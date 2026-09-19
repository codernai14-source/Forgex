package com.forgex.basic.employee.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人员分页查询参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeePageParam extends BaseGetParam {

    /** 工号。 */
    private String employeeNo;

    /** 姓名。 */
    private String employeeName;

    /** 手机号。 */
    private String phone;

    /** 部门 ID。 */
    private Long departmentId;

    /** 岗位 ID。 */
    private Long positionId;

    /** 是否启用。 */
    private Boolean status;
}

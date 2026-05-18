package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 人员第三方同步数据。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
public class EmployeeThirdPartySyncDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;

    private String employeeNo;

    private String employeeName;

    private String phone;

    private String email;

    private Integer gender;

    private String avatar;

    private LocalDate entryDate;

    private Long departmentId;

    private Long positionId;

    private Boolean status;

    private String remark;
}

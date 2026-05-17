package com.forgex.basic.employee.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 人员主数据实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_employee")
public class BasicEmployee extends BaseEntity {

    /** 工号。 */
    @TableField("employee_no")
    private String employeeNo;

    /** 姓名。 */
    @TableField("employee_name")
    private String employeeName;

    /** 手机号。 */
    @TableField("phone")
    private String phone;

    /** 邮箱。 */
    @TableField("email")
    private String email;

    /** 性别：0 未知，1 男，2 女。 */
    @TableField("gender")
    private Integer gender;

    /** 头像地址。 */
    @TableField("avatar")
    private String avatar;

    /** 入职日期。 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("entry_date")
    private LocalDate entryDate;

    /** 部门 ID。 */
    @TableField("department_id")
    private Long departmentId;

    /** 岗位 ID。 */
    @TableField("position_id")
    private Long positionId;

    /** 是否启用。 */
    @TableField("status")
    private Boolean status;

    /** 备注。 */
    @TableField("remark")
    private String remark;
}

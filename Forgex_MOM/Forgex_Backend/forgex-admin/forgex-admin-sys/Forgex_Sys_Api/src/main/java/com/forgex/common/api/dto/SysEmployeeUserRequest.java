package com.forgex.common.api.dto;

import java.time.LocalDate;

/**
 * 人员同步用户命令。仅携带人员主数据，密码、授权与用户配额由平台处理。
 *
 * @param employeeId 人员 ID，也是重复同步时的业务标识
 * @param tenantId 人员所属租户，必须与调用会话一致
 * @param employeeNo 工号，即平台账号
 * @param employeeName 姓名
 * @param phone 手机号
 * @param email 邮箱
 * @param gender 性别
 * @param avatar 头像
 * @param entryDate 入职日期
 * @param departmentId 部门 ID
 * @param positionId 岗位 ID
 * @param status 是否启用
 */
public record SysEmployeeUserRequest(Long employeeId, Long tenantId, String employeeNo,
        String employeeName, String phone, String email, Integer gender, String avatar,
        LocalDate entryDate, Long departmentId, Long positionId, Boolean status) {
}

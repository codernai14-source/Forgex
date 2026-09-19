package com.forgex.common.api.dto;

/**
 * 人员展示信息批量查询项。
 *
 * @param employeeId 人员 ID
 * @param departmentId 部门 ID
 * @param positionId 岗位 ID
 */
public record SysEmployeeReferenceRequest(Long employeeId, Long departmentId, Long positionId) {
}

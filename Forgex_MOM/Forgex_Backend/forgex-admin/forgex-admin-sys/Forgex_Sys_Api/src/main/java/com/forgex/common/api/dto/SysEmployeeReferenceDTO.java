package com.forgex.common.api.dto;

/**
 * 人员关联的平台信息。
 *
 * @param employeeId 人员 ID
 * @param departmentName 部门名称
 * @param positionName 岗位名称
 * @param userId 当前租户中绑定的用户 ID
 */
public record SysEmployeeReferenceDTO(Long employeeId, String departmentName, String positionName, Long userId) {
}

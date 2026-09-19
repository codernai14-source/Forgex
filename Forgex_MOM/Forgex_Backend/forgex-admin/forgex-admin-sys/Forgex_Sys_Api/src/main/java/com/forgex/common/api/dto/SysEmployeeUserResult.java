package com.forgex.common.api.dto;

/**
 * 人员同步用户结果，仅在用户与租户绑定均提交成功后返回。
 *
 * @param userId 平台用户 ID
 * @param created 本次是否新建用户；重复同步返回 false
 */
public record SysEmployeeUserResult(Long userId, boolean created) {
}

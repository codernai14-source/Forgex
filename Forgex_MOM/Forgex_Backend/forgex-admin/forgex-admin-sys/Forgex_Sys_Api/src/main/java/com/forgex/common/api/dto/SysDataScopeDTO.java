package com.forgex.common.api.dto;

import java.util.Set;

/**
 * 当前登录用户在当前租户下的数据权限快照。
 *
 * @param tenantId 已验证的租户 ID
 * @param userId 已验证的用户 ID
 * @param dataScope 数据范围编码
 * @param deptIds 可访问的部门 ID 集合
 */
public record SysDataScopeDTO(Long tenantId, Long userId, String dataScope, Set<Long> deptIds) {
}

package com.forgex.sys.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色权限DTO
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Data
public class RolePermissionDTO {
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 菜单ID列表（包含菜单和按钮权限）
     */
    private List<Long> menuIds;
}

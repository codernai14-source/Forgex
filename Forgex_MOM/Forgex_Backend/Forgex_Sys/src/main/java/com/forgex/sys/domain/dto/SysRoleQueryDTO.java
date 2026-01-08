package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 角色查询DTO
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Data
public class SysRoleQueryDTO {
    
    /**
     * 角色名称（模糊查询）
     */
    private String roleName;
    
    /**
     * 角色键（模糊查询）
     */
    private String roleKey;
    
    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;
    
    /**
     * 租户ID
     */
    private Long tenantId;
}

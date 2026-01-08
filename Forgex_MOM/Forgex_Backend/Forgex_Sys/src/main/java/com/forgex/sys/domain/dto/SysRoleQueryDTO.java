package com.forgex.sys.domain.dto;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询DTO
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleQueryDTO extends BaseGetParam {
    
    /**
     * 角色名称（模糊查询）
     */
    private String roleName;
    
    /**
     * 角色编码（模糊查询）
     */
    private String roleCode;
    
    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;
    
    /**
     * 租户ID
     */
    private Long tenantId;
}

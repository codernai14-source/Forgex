package com.forgex.sys.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色DTO
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Data
public class SysRoleDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色键
     */
    private String roleKey;
    
    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 租户ID
     */
    private Long tenantId;
}

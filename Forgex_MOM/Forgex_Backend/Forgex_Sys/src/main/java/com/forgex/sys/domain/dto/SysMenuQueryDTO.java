package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 菜单查询DTO
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Data
public class SysMenuQueryDTO {
    
    /**
     * 模块ID
     */
    private Long moduleId;
    
    /**
     * 菜单名称（模糊查询）
     */
    private String name;
    
    /**
     * 菜单类型：catalog=目录, menu=菜单, button=按钮
     */
    private String type;
    
    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;
    
    /**
     * 租户ID
     */
    private Long tenantId;
}

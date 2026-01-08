package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 模块查询DTO
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Data
public class SysModuleQueryDTO {
    
    /** 模块编码（模糊查询） */
    private String code;
    
    /** 模块名称（模糊查询） */
    private String name;
    
    /** 状态：0=禁用，1=启用 */
    private Integer status;
    
    /** 租户ID */
    private Long tenantId;
}

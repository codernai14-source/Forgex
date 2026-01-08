package com.forgex.sys.domain.dto;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模块查询DTO
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysModuleQueryDTO extends BaseGetParam {
    
    /** 模块编码（模糊查询） */
    private String code;
    
    /** 模块名称（模糊查询） */
    private String name;
    
    /** 状态：0=禁用，1=启用 */
    private Integer status;
    
    /** 租户ID */
    private Long tenantId;
}

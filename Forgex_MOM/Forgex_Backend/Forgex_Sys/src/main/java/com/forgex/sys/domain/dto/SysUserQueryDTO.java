package com.forgex.sys.domain.dto;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询DTO
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserQueryDTO extends BaseGetParam {
    
    /**
     * 账号（模糊查询）
     */
    private String account;
    
    /**
     * 用户名（模糊查询）
     */
    private String username;
    
    /**
     * 手机号（模糊查询）
     */
    private String phone;
    
    /**
     * 部门ID
     */
    private Long departmentId;
    
    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;
    
    /**
     * 租户ID
     */
    private Long tenantId;
}

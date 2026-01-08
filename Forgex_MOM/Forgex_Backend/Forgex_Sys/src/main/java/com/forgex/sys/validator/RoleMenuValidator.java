package com.forgex.sys.validator;

import com.forgex.common.exception.BusinessException;
import com.forgex.sys.domain.dto.RolePermissionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 角色菜单数据校验器
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Component
@RequiredArgsConstructor
public class RoleMenuValidator {
    
    /**
     * 查询角色菜单权限参数校验
     * 
     * @param roleId 角色ID
     * @param tenantId 租户ID
     */
    public void validateQueryParams(Long roleId, Long tenantId) {
        Assert.notNull(roleId, "角色ID不能为空");
        Assert.notNull(tenantId, "租户ID不能为空");
        
        if (roleId <= 0) {
            throw new BusinessException("角色ID格式不正确");
        }
        if (tenantId <= 0) {
            throw new BusinessException("租户ID格式不正确");
        }
    }
    
    /**
     * 授予权限参数校验
     * 
     * @param permissionDTO 权限信息
     */
    public void validateGrantParams(RolePermissionDTO permissionDTO) {
        Assert.notNull(permissionDTO, "权限信息不能为空");
        Assert.notNull(permissionDTO.getRoleId(), "角色ID不能为空");
        Assert.notNull(permissionDTO.getTenantId(), "租户ID不能为空");
        
        if (permissionDTO.getRoleId() <= 0) {
            throw new BusinessException("角色ID格式不正确");
        }
        if (permissionDTO.getTenantId() <= 0) {
            throw new BusinessException("租户ID格式不正确");
        }
    }
}

package com.forgex.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.RolePermissionDTO;
import com.forgex.sys.domain.entity.SysRoleMenu;

import java.util.List;

/**
 * 角色菜单Service接口
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
public interface ISysRoleMenuService extends IService<SysRoleMenu> {
    
    /**
     * 获取角色的菜单权限列表
     * 
     * @param roleId 角色ID
     * @param tenantId 租户ID
     * @return 菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId, Long tenantId);
    
    /**
     * 授予角色菜单权限
     * 
     * @param permissionDTO 权限信息
     */
    void grantPermission(RolePermissionDTO permissionDTO);
    
    /**
     * 删除角色的所有菜单权限
     * 
     * @param roleId 角色ID
     * @param tenantId 租户ID
     */
    void deleteRolePermissions(Long roleId, Long tenantId);
}

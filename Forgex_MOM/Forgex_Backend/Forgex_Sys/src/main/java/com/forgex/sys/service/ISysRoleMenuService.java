/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.RolePermissionDTO;
import com.forgex.sys.domain.entity.SysRoleMenu;

import java.util.List;

/**
 * 角色菜单Service接口
 * 
 * @author coder_nai@163.com
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

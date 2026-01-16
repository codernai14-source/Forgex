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
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.sys.domain.dto.RolePermissionDTO;
import com.forgex.sys.domain.entity.SysRoleMenu;
import com.forgex.sys.mapper.SysRoleMenuMapper;
import com.forgex.sys.service.ISysRoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色菜单Service实现类
 * <p>负责角色与菜单的关联关系管理，包括查询角色菜单权限、授予角色菜单权限、删除角色菜单权限等操作。</p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>查询角色拥有的菜单ID列表</li>
 *   <li>授予角色菜单权限（先删除原有权限，再插入新权限）</li>
 *   <li>删除角色菜单权限</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 * @see ISysRoleMenuService
 */
@Service
@RequiredArgsConstructor
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> 
    implements ISysRoleMenuService {
    
    private final SysRoleMenuMapper roleMenuMapper;
    
    /**
     * 查询角色拥有的菜单ID列表
     * <p>根据角色ID和租户ID查询角色拥有的所有菜单ID。</p>
     * @param roleId 角色ID
     * @param tenantId 租户ID
     * @return 角色拥有的菜单ID列表
     */
    @Override
    public List<Long> getRoleMenuIds(Long roleId, Long tenantId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(wrapper);
        return roleMenus.stream()
            .map(SysRoleMenu::getMenuId)
            .collect(Collectors.toList());
    }
    
    /**
     * 授予角色菜单权限
     * <p>先删除角色原有的菜单权限，再插入新的菜单权限。</p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>删除角色原有的菜单权限</li>
     *   <li>遍历新的菜单ID列表，为角色添加菜单权限</li>
     * </ol>
     * @param permissionDTO 角色权限DTO，包含角色ID、租户ID和菜单ID列表
     * @see #deleteRolePermissions(Long, Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantPermission(RolePermissionDTO permissionDTO) {
        // 1. 删除原有权限
        deleteRolePermissions(permissionDTO.getRoleId(), permissionDTO.getTenantId());
        
        // 2. 插入新权限
        if (permissionDTO.getMenuIds() != null && !permissionDTO.getMenuIds().isEmpty()) {
            for (Long menuId : permissionDTO.getMenuIds()) {
                if (menuId == null) {
                    continue;
                }
                
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(permissionDTO.getRoleId());
                roleMenu.setTenantId(permissionDTO.getTenantId());
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }
    }
    
    /**
     * 删除角色菜单权限
     * <p>根据角色ID和租户ID删除角色的所有菜单权限。</p>
     * @param roleId 角色ID
     * @param tenantId 租户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRolePermissions(Long roleId, Long tenantId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        wrapper.eq(SysRoleMenu::getTenantId, tenantId);
        roleMenuMapper.delete(wrapper);
    }
}

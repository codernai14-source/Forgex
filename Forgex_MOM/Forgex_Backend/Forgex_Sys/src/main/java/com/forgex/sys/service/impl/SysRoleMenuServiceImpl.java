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
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Service
@RequiredArgsConstructor
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> 
    implements ISysRoleMenuService {
    
    private final SysRoleMenuMapper roleMenuMapper;
    
    @Override
    public List<Long> getRoleMenuIds(Long roleId, Long tenantId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        wrapper.eq(SysRoleMenu::getTenantId, tenantId);
        
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(wrapper);
        return roleMenus.stream()
            .map(SysRoleMenu::getMenuId)
            .collect(Collectors.toList());
    }
    
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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRolePermissions(Long roleId, Long tenantId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        wrapper.eq(SysRoleMenu::getTenantId, tenantId);
        roleMenuMapper.delete(wrapper);
    }
}

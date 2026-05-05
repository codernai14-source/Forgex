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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色菜单 Service 实现类
 * <p>
 * 负责角色与菜单的关联关系管理，包括查询角色菜单权限、授予角色菜单权限、
 * 删除角色菜单权限等操作。
 * </p>
 *
 * @author coder_nai@163.com
 * @date 2025-01-07
 * @see ISysRoleMenuService
 *
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu>
    implements ISysRoleMenuService {

    private final SysRoleMenuMapper roleMenuMapper;

    /**
     * 查询角色拥有的菜单 ID 列表
     * <p>
     * 按角色和租户双维度过滤，避免跨租户菜单授权回显串数据。
     * </p>
     *
     * @param roleId 角色 ID
     * @param tenantId 租户 ID
     * @return 角色拥有的菜单 ID 列表
     */
    @Override
    public List<Long> getRoleMenuIds(Long roleId, Long tenantId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        wrapper.eq(SysRoleMenu::getTenantId, tenantId);

        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(wrapper);
        return roleMenus.stream()
            .map(SysRoleMenu::getMenuId)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 授予角色菜单权限
     * <p>
     * 采用先删除后新增的覆盖式写法，并在写入前对菜单 ID 去重，
     * 避免重复授权导致脏数据。
     * </p>
     *
     * @param permissionDTO 角色权限 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantPermission(RolePermissionDTO permissionDTO) {
        // 1. 先删除当前租户下该角色原有的菜单授权
        deleteRolePermissions(permissionDTO.getRoleId(), permissionDTO.getTenantId());

        // 2. 再插入新的菜单授权，并对菜单 ID 做去重处理
        if (permissionDTO.getMenuIds() != null && !permissionDTO.getMenuIds().isEmpty()) {
            for (Long menuId : new LinkedHashSet<>(permissionDTO.getMenuIds())) {
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
     *
     * @param roleId 角色 ID
     * @param tenantId 租户 ID
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

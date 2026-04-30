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
package com.forgex.sys.validator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.RolePermissionDTO;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.mapper.SysMenuMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色菜单数据校验器
 *
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Component
@RequiredArgsConstructor
public class RoleMenuValidator {

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    /**
     * 查询角色菜单权限参数校验
     *
     * @param roleId 角色 ID
     * @param tenantId 租户 ID
     */
    public void validateQueryParams(Long roleId, Long tenantId) {
        Assert.notNull(roleId, "角色 ID 不能为空");
        Assert.notNull(tenantId, "租户 ID 不能为空");

        if (roleId <= 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ROLE_MENU_ROLE_ID_INVALID);
        }
        if (tenantId <= 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ROLE_MENU_TENANT_ID_INVALID);
        }
    }

    /**
     * 授予权限参数校验
     *
     * @param permissionDTO 权限信息
     */
    public void validateGrantParams(RolePermissionDTO permissionDTO) {
        Assert.notNull(permissionDTO, "权限信息不能为空");
        Assert.notNull(permissionDTO.getRoleId(), "角色 ID 不能为空");
        Assert.notNull(permissionDTO.getTenantId(), "租户 ID 不能为空");

        if (permissionDTO.getRoleId() <= 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ROLE_MENU_ROLE_ID_INVALID);
        }
        if (permissionDTO.getTenantId() <= 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ROLE_MENU_TENANT_ID_INVALID);
        }

        validateRoleInTenant(permissionDTO.getRoleId(), permissionDTO.getTenantId());
        validateMenusInTenant(permissionDTO.getMenuIds(), permissionDTO.getTenantId());
    }

    private void validateRoleInTenant(Long roleId, Long tenantId) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getId, roleId)
            .eq(SysRole::getTenantId, tenantId)
            .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ROLE_NOT_FOUND);
        }
    }

    private void validateMenusInTenant(List<Long> menuIds, Long tenantId) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }

        Set<Long> distinctMenuIds = new LinkedHashSet<>();
        for (Long menuId : menuIds) {
            if (menuId == null || menuId <= 0) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_NOT_FOUND);
            }
            distinctMenuIds.add(menuId);
        }

        Long validCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
            .in(SysMenu::getId, distinctMenuIds)
            .eq(SysMenu::getTenantId, tenantId)
            .eq(SysMenu::getDeleted, false));
        if (validCount == null || validCount.intValue() != distinctMenuIds.size()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_NOT_FOUND);
        }
    }
}

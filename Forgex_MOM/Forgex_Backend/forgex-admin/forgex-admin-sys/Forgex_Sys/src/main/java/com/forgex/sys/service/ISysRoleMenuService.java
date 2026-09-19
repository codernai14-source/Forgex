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
 * 角色菜单 Service 接口
 * <p>
 * 提供角色菜单权限管理相关的业务操作，包括查询角色菜单权限、授予角色菜单权限、删除角色菜单权限等功能。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #getRoleMenuIds(Long, Long)} - 查询角色拥有的菜单 ID 列表</li>
 *   <li>{@link #grantPermission(RolePermissionDTO)} - 授予角色菜单权限</li>
 *   <li>{@link #deleteRolePermissions(Long, Long)} - 删除角色的所有菜单权限</li>
 * </ul>
 * <p>使用说明：</p>
 * <ul>
 *   <li>授予权限时采用先删除后插入的策略，确保权限数据的准确性</li>
 *   <li>所有写操作（授予权限、删除权限）均使用@Transactional 保证事务一致性</li>
 *   <li>租户 ID 用于多租户数据隔离</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2025-01-07
 * @see com.forgex.sys.service.impl.SysRoleMenuServiceImpl
 * @see RolePermissionDTO
 * @see SysRoleMenu
 */
public interface ISysRoleMenuService extends IService<SysRoleMenu> {
    
    /**
     * 查询角色拥有的菜单 ID 列表
     * <p>
     * 根据角色 ID 和租户 ID 查询角色拥有的所有菜单 ID。
     * </p>
     * 
     * @param roleId 角色 ID，必填参数
     * @param tenantId 租户 ID，必填参数
     * @return 角色拥有的菜单 ID 列表
     * @see RolePermissionDTO
     */
    List<Long> getRoleMenuIds(Long roleId, Long tenantId);
    
    /**
     * 授予角色菜单权限
     * <p>
     * 为角色授予菜单权限，采用先删除后插入的策略。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>删除角色原有的菜单权限</li>
     *   <li>遍历新的菜单 ID 列表，为角色添加菜单权限</li>
     * </ol>
     * 
     * @param permissionDTO 角色权限 DTO，包含角色 ID、租户 ID 和菜单 ID 列表
     * @throws IllegalArgumentException 当参数为空时抛出
     * @see RolePermissionDTO
     * @see #deleteRolePermissions(Long, Long)
     */
    void grantPermission(RolePermissionDTO permissionDTO);
    
    /**
     * 删除角色的所有菜单权限
     * <p>
     * 根据角色 ID 和租户 ID 删除角色的所有菜单权限。
     * </p>
     * 
     * @param roleId 角色 ID，必填参数
     * @param tenantId 租户 ID，必填参数
     * @throws IllegalArgumentException 当参数为空时抛出
     */
    void deleteRolePermissions(Long roleId, Long tenantId);
}

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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.SysRoleDTO;
import com.forgex.sys.domain.dto.SysRoleQueryDTO;
import com.forgex.sys.domain.entity.SysRole;

import java.util.List;

/**
 * 角色Service接口
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
public interface ISysRoleService extends IService<SysRole> {
    
    /**
     * 分页查询角色列表
     * 
     * @param page 分页参数
     * @param query 查询条件
     * @return 角色分页数据
     */
    IPage<SysRoleDTO> pageRoles(Page<SysRole> page, SysRoleQueryDTO query);
    
    /**
     * 查询角色列表
     * 
     * @param query 查询条件
     * @return 角色列表
     */
    List<SysRoleDTO> listRoles(SysRoleQueryDTO query);
    
    /**
     * 根据ID获取角色详情
     * 
     * @param id 角色ID
     * @return 角色详情
     */
    SysRoleDTO getRoleById(Long id);
    
    /**
     * 新增角色
     * 
     * @param roleDTO 角色信息
     */
    void addRole(SysRoleDTO roleDTO);
    
    /**
     * 更新角色
     * 
     * @param roleDTO 角色信息
     */
    void updateRole(SysRoleDTO roleDTO);
    
    /**
     * 删除角色
     * 
     * @param id 角色ID
     */
    void deleteRole(Long id);
    
    /**
     * 批量删除角色
     * 
     * @param ids 角色ID列表
     */
    void batchDeleteRoles(List<Long> ids);
    
    /**
     * 检查角色是否存在
     * 
     * @param id 角色ID
     * @return 是否存在
     */
    boolean existsById(Long id);
    
    /**
     * 检查角色键是否存在
     * 
     * @param roleKey 角色键
     * @return 是否存在
     */
    boolean existsByRoleKey(String roleKey);
    
    /**
     * 检查角色键是否存在（排除指定ID）
     * 
     * @param roleKey 角色键
     * @param excludeId 排除的角色ID
     * @return 是否存在
     */
    boolean existsByRoleKeyExcludeId(String roleKey, Long excludeId);
    
    /**
     * 检查角色编码是否存在
     * 
     * @param roleCode 角色编码
     * @param tenantId 租户ID
     * @return 是否存在
     */
    boolean existsByRoleCode(String roleCode, Long tenantId);
    
    /**
     * 检查角色编码是否存在（排除指定ID）
     * 
     * @param roleCode 角色编码
     * @param tenantId 租户ID
     * @param excludeId 排除的角色ID
     * @return 是否存在
     */
    boolean existsByRoleCodeExcludeId(String roleCode, Long tenantId, Long excludeId);
    
    /**
     * 检查角色名称是否存在
     * 
     * @param roleName 角色名称
     * @param tenantId 租户ID
     * @return 是否存在
     */
    boolean existsByRoleName(String roleName, Long tenantId);
    
    /**
     * 检查角色名称是否存在（排除指定ID）
     * 
     * @param roleName 角色名称
     * @param tenantId 租户ID
     * @param excludeId 排除的角色ID
     * @return 是否存在
     */
    boolean existsByRoleNameExcludeId(String roleName, Long tenantId, Long excludeId);
    
    /**
     * 检查角色是否有关联用户
     * 
     * @param id 角色ID
     * @return 是否有关联用户
     */
    boolean hasUserAssociation(Long id);
    
    /**
     * 根据ID获取角色编码
     * 
     * @param id 角色ID
     * @return 角色编码
     */
    String getRoleCodeById(Long id);
}

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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.sys.domain.dto.SysRoleDTO;
import com.forgex.sys.domain.dto.SysRoleQueryDTO;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色Service实现类
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> 
    implements ISysRoleService {
    
    private final SysRoleMapper roleMapper;
    private final com.forgex.sys.mapper.SysRoleMenuMapper roleMenuMapper;
    private final com.forgex.sys.mapper.SysUserRoleMapper userRoleMapper;
    
    /**
     * 分页查询角色列表
     * <p>
     * 根据查询条件分页获取角色列表，并转换为DTO对象
     * </p>
     * 
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页角色列表
     */
    @Override
    public IPage<SysRoleDTO> pageRoles(Page<SysRole> page, SysRoleQueryDTO query) {
        LambdaQueryWrapper<SysRole> wrapper = buildQueryWrapper(query);
        IPage<SysRole> rolePage = roleMapper.selectPage(page, wrapper);
        return rolePage.convert(this::convertToDTO);
    }
    
    /**
     * 查询角色列表
     * <p>
     * 根据查询条件获取角色列表，并转换为DTO对象
     * </p>
     * 
     * @param query 查询条件
     * @return 角色列表
     */
    @Override
    public List<SysRoleDTO> listRoles(SysRoleQueryDTO query) {
        LambdaQueryWrapper<SysRole> wrapper = buildQueryWrapper(query);
        List<SysRole> roles = roleMapper.selectList(wrapper);
        return roles.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * 根据ID获取角色详情
     * <p>
     * 根据角色ID查询角色信息，并转换为DTO对象
     * </p>
     * 
     * @param id 角色ID
     * @return 角色DTO对象，不存在则返回null
     */
    @Override
    public SysRoleDTO getRoleById(Long id) {
        SysRole role = roleMapper.selectById(id);
        return role != null ? convertToDTO(role) : null;
    }
    
    /**
     * 添加角色
     * <p>
     * 根据角色DTO创建新的角色记录
     * </p>
     * 
     * @param roleDTO 角色DTO对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(SysRoleDTO roleDTO) {
        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleDTO, role);
        role.setRoleKey(roleDTO.getRoleCode());
        roleMapper.insert(role);
    }
    
    /**
     * 更新角色
     * <p>
     * 根据角色DTO更新现有角色记录
     * </p>
     * 
     * @param roleDTO 角色DTO对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRoleDTO roleDTO) {
        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleDTO, role);
        role.setRoleKey(roleDTO.getRoleCode());
        roleMapper.updateById(role);
    }
    
    /**
     * 删除角色（任务 16：添加级联删除逻辑）
     * <p>
     * 根据角色ID删除角色记录，删除前先删除 sys_role_menu 表中的关联记录
     * </p>
     * 
     * @param id 角色ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        // 1. 先删除 sys_role_menu 表中的关联记录（任务 16）
        LambdaQueryWrapper<com.forgex.sys.domain.entity.SysRoleMenu> wrapper = 
            new LambdaQueryWrapper<>();
        wrapper.eq(com.forgex.sys.domain.entity.SysRoleMenu::getRoleId, id);
        roleMenuMapper.delete(wrapper);
        
        // 2. 再删除角色记录
        roleMapper.deleteById(id);
    }
    
    /**
     * 批量删除角色
     * <p>
     * 批量删除多个角色记录
     * </p>
     * 
     * @param ids 角色ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteRoles(List<Long> ids) {
        // 1. 先删除 sys_role_menu 表中的关联记录（任务 16）
        LambdaQueryWrapper<com.forgex.sys.domain.entity.SysRoleMenu> wrapper = 
            new LambdaQueryWrapper<>();
        wrapper.in(com.forgex.sys.domain.entity.SysRoleMenu::getRoleId, ids);
        roleMenuMapper.delete(wrapper);
        
        // 2. 再删除角色记录
        removeByIds(ids);
    }
    
    /**
     * 检查角色是否存在
     * <p>
     * 根据角色ID检查角色是否存在
     * </p>
     * 
     * @param id 角色ID
     * @return 角色是否存在
     */
    @Override
    public boolean existsById(Long id) {
        return roleMapper.selectById(id) != null;
    }
    
    /**
     * 检查角色标识是否存在
     * <p>
     * 根据角色标识检查角色是否存在
     * </p>
     * 
     * @param roleKey 角色标识
     * @return 角色标识是否存在
     */
    @Override
    public boolean existsByRoleKey(String roleKey) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, roleKey);
        return roleMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查角色标识是否存在（排除指定ID）
     * <p>
     * 根据角色标识检查角色是否存在，排除指定的角色ID
     * </p>
     * 
     * @param roleKey   角色标识
     * @param excludeId 排除的角色ID
     * @return 角色标识是否存在（排除指定ID后）
     */
    @Override
    public boolean existsByRoleKeyExcludeId(String roleKey, Long excludeId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, roleKey);
        wrapper.ne(SysRole::getId, excludeId);
        return roleMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查角色编码是否存在
     * <p>
     * 根据角色编码和租户ID检查角色是否存在
     * </p>
     * 
     * @param roleCode 角色编码
     * @param tenantId 租户ID
     * @return 角色编码是否存在
     */
    @Override
    public boolean existsByRoleCode(String roleCode, Long tenantId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, roleCode);
        return roleMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查角色编码是否存在（排除指定ID）
     * <p>
     * 根据角色编码和租户ID检查角色是否存在，排除指定的角色ID
     * </p>
     * 
     * @param roleCode 角色编码
     * @param tenantId 租户ID
     * @param excludeId 排除的角色ID
     * @return 角色编码是否存在（排除指定ID后）
     */
    @Override
    public boolean existsByRoleCodeExcludeId(String roleCode, Long tenantId, Long excludeId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, roleCode);
        wrapper.ne(SysRole::getId, excludeId);
        return roleMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查角色名称是否存在
     * <p>
     * 根据角色名称和租户ID检查角色是否存在
     * </p>
     * 
     * @param roleName 角色名称
     * @param tenantId 租户ID
     * @return 角色名称是否存在
     */
    @Override
    public boolean existsByRoleName(String roleName, Long tenantId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleName, roleName);
        return roleMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查角色名称是否存在（排除指定ID）
     * <p>
     * 根据角色名称和租户ID检查角色是否存在，排除指定的角色ID
     * </p>
     * 
     * @param roleName 角色名称
     * @param tenantId 租户ID
     * @param excludeId 排除的角色ID
     * @return 角色名称是否存在（排除指定ID后）
     */
    @Override
    public boolean existsByRoleNameExcludeId(String roleName, Long tenantId, Long excludeId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleName, roleName);
        wrapper.ne(SysRole::getId, excludeId);
        return roleMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查角色是否有关联用户
     * <p>
     * 根据角色ID检查是否存在关联的用户
     * </p>
     * 
     * @param id 角色ID
     * @return 是否有关联用户
     */
    @Override
    public boolean hasUserAssociation(Long id) {
        LambdaQueryWrapper<com.forgex.sys.domain.entity.SysUserRole> wrapper = 
            new LambdaQueryWrapper<>();
        wrapper.eq(com.forgex.sys.domain.entity.SysUserRole::getRoleId, id);
        return userRoleMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 根据ID获取角色编码
     * <p>
     * 根据角色ID查询角色编码
     * </p>
     * 
     * @param id 角色ID
     * @return 角色编码，不存在则返回null
     */
    @Override
    public String getRoleCodeById(Long id) {
        SysRole role = roleMapper.selectById(id);
        return role != null ? role.getRoleKey() : null;
    }
    
    /**
     * 构建角色查询条件
     * <p>
     * 根据查询DTO构建角色查询条件
     * </p>
     * 
     * @param query 查询DTO
     * @return 角色查询条件
     */
    private LambdaQueryWrapper<SysRole> buildQueryWrapper(SysRoleQueryDTO query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        Long tenantId = resolveQueryTenantId(query);
        if (tenantId != null) {
            wrapper.eq(SysRole::getTenantId, tenantId);
        }

        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getRoleName()), 
                SysRole::getRoleName, query.getRoleName());
            wrapper.like(StringUtils.hasText(query.getRoleCode()), 
                SysRole::getRoleKey, query.getRoleCode());
            wrapper.eq(query.getStatus() != null, 
                SysRole::getStatus, query.getStatus());
        }
        
        wrapper.orderByDesc(SysRole::getCreateTime);
        return wrapper;
    }

    private Long resolveQueryTenantId(SysRoleQueryDTO query) {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            return tenantId;
        }
        tenantId = CurrentUserUtils.getTenantId();
        if (tenantId != null) {
            return tenantId;
        }
        return query == null ? null : query.getTenantId();
    }

    /**
     * 角色实体转DTO对象
     * <p>
     * 将SysRole实体转换为SysRoleDTO对象
     * </p>
     * 
     * @param role 角色实体
     * @return 角色DTO对象
     */
    private SysRoleDTO convertToDTO(SysRole role) {
        SysRoleDTO dto = new SysRoleDTO();
        BeanUtils.copyProperties(role, dto);
        // 手动映射：roleKey -> roleCode（前端使用roleCode字段）
        dto.setRoleCode(role.getRoleKey());
        return dto;
    }
}
